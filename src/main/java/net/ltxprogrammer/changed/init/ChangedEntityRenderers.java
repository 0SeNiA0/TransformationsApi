package net.ltxprogrammer.changed.init;

import com.google.common.collect.ImmutableMap;
import net.ltxprogrammer.changed.client.RegisterComplexRenderersEvent;
import net.ltxprogrammer.changed.client.renderer.DarkLatexWolfFemaleRenderer;
import net.ltxprogrammer.changed.client.renderer.DarkLatexWolfMaleRenderer;
import net.ltxprogrammer.changed.client.renderer.DarkLatexWolfPupRenderer;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.ComplexRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChangedEntityRenderers {
    private static final List<EntityType<? extends ChangedEntity>> copyPlayerLayers = new ArrayList<>();
    private static Map<EntityType<? extends ComplexRenderer>, Map<String, EntityRenderer<? extends ComplexRenderer>>> complexRenderers = ImmutableMap.of();

    public static <T extends Entity> Optional<EntityRenderer<? super T>> getComplexRenderer(T entity) {
        if (!(entity instanceof ComplexRenderer complexRenderer))
            return Optional.empty();

        final var renderers = Optional.ofNullable(complexRenderers.get(entity.getType()));
        return renderers.map(map -> (EntityRenderer<? super T>)map.getOrDefault(complexRenderer.getModelName(), map.get("default")));
    }

    public static List<EntityType<? extends ChangedEntity>> getCopyPlayerLayers() {
        return copyPlayerLayers;
    }

    public static Stream<EntityRenderer<? extends ComplexRenderer>> getComplexRenderers(String name) {
        return complexRenderers.values().stream().map(entityRenderers -> entityRenderers.get(name));
    }

    public static void registerComplexRenderers(EntityRendererProvider.Context context) {
        final var event = new RegisterComplexRenderersEvent();
        ModLoader.get().postEvent(event);
        complexRenderers = event.build(context);
    }

    private static <T extends ChangedEntity> void registerHumanoid(EntityRenderersEvent.RegisterRenderers event, EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
        copyPlayerLayers.add(entityType);
        event.registerEntityRenderer(entityType, entityRendererProvider);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        copyPlayerLayers.clear();

        registerHumanoid(event, ChangedEntities.DARK_LATEX_WOLF_FEMALE.get(), DarkLatexWolfFemaleRenderer::new);
        registerHumanoid(event, ChangedEntities.DARK_LATEX_WOLF_MALE.get(), DarkLatexWolfMaleRenderer::new);
        registerHumanoid(event, ChangedEntities.DARK_LATEX_WOLF_PUP.get(), DarkLatexWolfPupRenderer::new);
    }
}
