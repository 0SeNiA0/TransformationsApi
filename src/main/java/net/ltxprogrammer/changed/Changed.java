package net.ltxprogrammer.changed;

import net.ltxprogrammer.changed.client.ChangedClient;
import net.ltxprogrammer.changed.client.EventHandlerClient;
import net.ltxprogrammer.changed.data.BuiltinRepositorySource;
import net.ltxprogrammer.changed.entity.AccessoryEntities;
import net.ltxprogrammer.changed.extension.ChangedCompatibility;
import net.ltxprogrammer.changed.init.*;
import net.ltxprogrammer.changed.network.ChangedPackets;
import net.ltxprogrammer.changed.network.packet.ChangedPacket;
import net.ltxprogrammer.changed.transform.HairStyle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(Changed.MODID)
public class Changed {
    private static Changed instance;
    public static Changed getInstance() { return instance; }

    public static final String MODID = "changed";

    public static final Logger LOGGER = LogManager.getLogger(Changed.class);
    public static EventHandlerClient eventHandlerClient;
    public static ChangedConfig config;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(modResource(MODID), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static final ChangedPackets PACKETS = new ChangedPackets(PACKET_HANDLER);
    private static int messageID = 0;

    /**
     * This function is split out of the main function as a request by mod extension devs
     */
    private void registerLoadingEventListeners(IEventBus eventBus) {
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::customPacks);
    }

    public Changed() {
        config = new ChangedConfig(ModLoadingContext.get());

        registerLoadingEventListeners(FMLJavaModLoadingContext.get().getModEventBus());

        addEventListener(this::dataListeners);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerClientEventListeners);

        PACKETS.registerPackets();

        instance = this;

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        HairStyle.REGISTRY.register(modEventBus);
        ChangedAbilities.REGISTRY.register(modEventBus);

        ChangedAttributes.REGISTRY.register(modEventBus);
        ChangedEnchantments.REGISTRY.register(modEventBus);
        ChangedTransfurVariants.REGISTRY.register(modEventBus);
        ChangedEntities.REGISTRY.register(modEventBus);
        ChangedAnimationEvents.REGISTRY.register(modEventBus);
        ChangedAccessorySlots.REGISTRY.register(modEventBus);
    }

    private void registerClientEventListeners() {
        MinecraftForge.EVENT_BUS.register(eventHandlerClient = new EventHandlerClient());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ChangedClient.registerEventListeners();
    }

    private void dataListeners(final AddReloadListenerEvent event) {
        event.addListener(ChangedFusions.INSTANCE);
        event.addListener(AccessoryEntities.INSTANCE);
        ChangedCompatibility.addDataListeners(event);
    }

    private void customPacks(final AddPackFindersEvent event) {
        try {
            switch (event.getPackType()) {
                case CLIENT_RESOURCES, SERVER_DATA ->
                        event.addRepositorySource(new BuiltinRepositorySource(event.getPackType(), MODID));
                default -> {}
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    private static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                             BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID++, messageType, encoder, decoder, messageConsumer);
    }

    private static <T extends ChangedPacket> void addNetworkMessage(Class<T> messageType, Function<FriendlyByteBuf, T> ctor) {
        addNetworkMessage(messageType, T::write, ctor, T::handle);
    }

    public static ResourceLocation modResource(String path) {
        return new ResourceLocation(MODID, path);
    }
    public static String modResourceStr(String path) {
        return MODID + ":" + path;
    }

    public static <T extends Event & IModBusEvent> void postModLoadingEvent(T event) {
        ModLoader.get().postEvent(event);
    }

    public static <T extends Event> void addEventListener(Consumer<T> listener) {
        MinecraftForge.EVENT_BUS.addListener(listener);
    }

    public static <T extends Event> boolean postModEvent(T event) {
        return MinecraftForge.EVENT_BUS.post(event);
    }

    public static <T extends Event> boolean postModEvent(T event, IEventBusInvokeDispatcher dispatcher) {
        return MinecraftForge.EVENT_BUS.post(event, dispatcher);
    }
}
