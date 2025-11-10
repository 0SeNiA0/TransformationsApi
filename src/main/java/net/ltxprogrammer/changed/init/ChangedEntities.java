package net.ltxprogrammer.changed.init;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.api.ChangedEntity;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfFemale;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfMale;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfPup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.ltxprogrammer.changed.transform.TransfurVariant.getNextEntId;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChangedEntities {
    public interface VoidConsumer { void accept(); }

    private static final Map<ResourceLocation, Pair<Integer, Integer>> ENTITY_COLOR_MAP = new HashMap<>();
    private static final List<Pair<Supplier<EntityType<? extends ChangedEntity>>, Supplier<AttributeSupplier.Builder>>> ATTR_FUNC_REGISTRY = new ArrayList<>();
    private static final List<VoidConsumer> INIT_FUNC_REGISTRY = new ArrayList<>();
    private static final Map<Level, Map<EntityType<?>, Entity>> CACHED_ENTITIES = new HashMap<>();

    public static Pair<Integer, Integer> getEntityColor(ResourceLocation location) {
        return ENTITY_COLOR_MAP.computeIfAbsent(location, loc -> {
            try {
                if (Registry.ITEM.get(new ResourceLocation(loc.getNamespace(), loc.getPath() + "_spawn_egg")) instanceof ForgeSpawnEggItem item)
                    return new Pair<>(item.getColor(0), item.getColor(1));
                else
                    return new Pair<>(0xF0F0F0, 0xF0F0F0);
            } catch (Exception ex) {
                return new Pair<>(0xF0F0F0, 0xF0F0F0);
            }
        });
    }

    public static int getEntityColorBack(ResourceLocation location) {
        return getEntityColor(location).getFirst();
    }

    public static int getEntityColorFront(ResourceLocation location) {
        return getEntityColor(location).getSecond();
    }

    public static <T extends Entity> T getCachedEntity(Level level, EntityType<T> type) {
        return (T)CACHED_ENTITIES.computeIfAbsent(level, (ignored) -> new HashMap<>()).computeIfAbsent(type, (entityType) -> {
            var entity = entityType.create(level);
            entity.setId(getNextEntId()); //to prevent ID collision
            entity.setSilent(true);
            return entity;
        });
    }

    public static void clearAllCachedEntities() {
        CACHED_ENTITIES.clear();
    }

    public static boolean overworldOnly(Level level) {
        return level.dimension().equals(Level.OVERWORLD);
    }

    public static boolean anyDimension(DimensionType dimensionType) {
        return true;
    }

    public static final Map<Supplier<? extends EntityType<?>>, Predicate<Level>> DIMENSION_RESTRICTIONS = new HashMap<>();
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, Changed.MODID);
    public static final Map<RegistryObject<? extends EntityType<?>>, RegistryObject<ForgeSpawnEggItem>> SPAWN_EGGS = new HashMap<>();

    public static final RegistryObject<EntityType<DarkLatexWolfMale>> DARK_LATEX_WOLF_MALE = registerSpawning("dark_latex_wolf_male", 0x393939, 0x303030,
            EntityType.Builder.of(DarkLatexWolfMale::new, ChangedMobCategories.CHANGED).clientTrackingRange(10).sized(0.7F, 1.93F),
            ChangedEntities::overworldOnly, SpawnPlacements.Type.ON_GROUND, DarkLatexWolfMale::checkEntitySpawnRules);
    public static final RegistryObject<EntityType<DarkLatexWolfFemale>> DARK_LATEX_WOLF_FEMALE = registerSpawning("dark_latex_wolf_female", 0x393939, 0x303030,
            EntityType.Builder.of(DarkLatexWolfFemale::new, ChangedMobCategories.CHANGED).clientTrackingRange(10).sized(0.7F, 1.93F),
            ChangedEntities::overworldOnly, SpawnPlacements.Type.ON_GROUND, DarkLatexWolfFemale::checkEntitySpawnRules);
    public static final RegistryObject<EntityType<DarkLatexWolfPup>> DARK_LATEX_WOLF_PUP = registerSpawning("dark_latex_wolf_pup", 0x454545, 0x303030,
            EntityType.Builder.of(DarkLatexWolfPup::new, ChangedMobCategories.CHANGED).clientTrackingRange(10).sized(0.6F, 0.85F),
            ChangedEntities::overworldOnly, SpawnPlacements.Type.ON_GROUND, DarkLatexWolfPup::checkEntitySpawnRules);

    // TODO make register function for non `ChangedEntity`

    private static <T extends ChangedEntity> RegistryObject<EntityType<T>> registerNoEgg(
            String name,
            int eggBack,
            int eggHighlight,
            EntityType.Builder<T> builder) {
        String regName = Changed.modResource(name).toString();
        ENTITY_COLOR_MAP.put(Changed.modResource(name), new Pair<>(eggBack, eggHighlight));
        RegistryObject<EntityType<T>> entityType = REGISTRY.register(name, () -> builder.build(regName));
        ATTR_FUNC_REGISTRY.add(new Pair<>(entityType::get, T::createLatexAttributes));
        return entityType;
    }

    private static <T extends ChangedEntity> RegistryObject<EntityType<T>> registerNoEgg(
            String name,
            EntityType.Builder<T> builder) {
        String regName = Changed.modResource(name).toString();
        RegistryObject<EntityType<T>> entityType = REGISTRY.register(name, () -> builder.build(regName));
        ATTR_FUNC_REGISTRY.add(new Pair<>(entityType::get, T::createLatexAttributes));
        return entityType;
    }

    private static <T extends ChangedEntity> RegistryObject<EntityType<T>> registerSpawning(
            String name,
            int eggBack,
            int eggHighlight,
            EntityType.Builder<T> builder,
            Predicate<Level> dimension,
            SpawnPlacements.Type spawnType,
            SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
        return registerSpawning(name, eggBack, eggHighlight, builder, dimension, spawnType, spawnPredicate, T::createLatexAttributes);
    }

    private static <T extends ChangedEntity> RegistryObject<EntityType<T>> registerSpawning(
            String name,
            int eggBack,
            int eggHighlight,
            EntityType.Builder<T> builder,
            Predicate<Level> dimension,
            SpawnPlacements.Type spawnType,
            SpawnPlacements.SpawnPredicate<T> spawnPredicate,
            Supplier<AttributeSupplier.Builder> attributes) {
        ENTITY_COLOR_MAP.put(Changed.modResource(name), new Pair<>(eggBack, eggHighlight));
        String regName = Changed.modResource(name).toString();
        RegistryObject<EntityType<T>> entityType = REGISTRY.register(name, () -> builder.build(regName));
        INIT_FUNC_REGISTRY.add(ChangedEntity.getInit(entityType, spawnType, spawnPredicate));
        ATTR_FUNC_REGISTRY.add(new Pair<>(entityType::get, attributes));
        DIMENSION_RESTRICTIONS.put(entityType, dimension);
        return entityType;
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> INIT_FUNC_REGISTRY.forEach(VoidConsumer::accept));
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        ATTR_FUNC_REGISTRY.forEach((pair) -> event.put(pair.getFirst().get(), pair.getSecond().get().build()));
    }
}
