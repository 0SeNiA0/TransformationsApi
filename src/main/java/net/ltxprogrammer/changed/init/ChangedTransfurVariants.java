package net.ltxprogrammer.changed.init;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.entity.ChangedEntity;
import net.ltxprogrammer.changed.entity.TransfurMode;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfFemale;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfMale;
import net.ltxprogrammer.changed.entity.beast.DarkLatexWolfPup;
import net.ltxprogrammer.changed.entity.variant.GenderedPair;
import net.ltxprogrammer.changed.entity.variant.TransfurVariant;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChangedTransfurVariants {
    public static final DeferredRegister<TransfurVariant<?>> REGISTRY = ChangedRegistry.TRANSFUR_VARIANT.createDeferred(Changed.MODID);

    public static final RegistryObject<TransfurVariant<DarkLatexWolfFemale>> DARK_LATEX_WOLF_FEMALE = register("form_dark_latex_wolf/female",
            TransfurVariant.Builder.of(ChangedEntities.DARK_LATEX_WOLF_FEMALE).stepSize(0.7f).scares(AbstractSkeleton.class).addAbility(ChangedAbilities.TOGGLE_WAVE_VISION).absorbing());
    public static final RegistryObject<TransfurVariant<DarkLatexWolfMale>> DARK_LATEX_WOLF_MALE = register("form_dark_latex_wolf/male",
            TransfurVariant.Builder.of(ChangedEntities.DARK_LATEX_WOLF_MALE).stepSize(0.7f).scares(AbstractSkeleton.class).addAbility(ChangedAbilities.TOGGLE_WAVE_VISION));
    public static final RegistryObject<TransfurVariant<DarkLatexWolfPup>> DARK_LATEX_WOLF_PUP = register("form_dark_latex_wolf_pup",
            TransfurVariant.Builder.of(ChangedEntities.DARK_LATEX_WOLF_PUP).stepSize(0.7f).scares(AbstractSkeleton.class).weakMining().addAbility(ChangedAbilities.TOGGLE_WAVE_VISION).transfurMode(TransfurMode.NONE).holdItemsInMouth().reducedFall().addAbility(ChangedAbilities.PUDDLE));

    public static final Supplier<? extends TransfurVariant<?>> FALLBACK_VARIANT = DARK_LATEX_WOLF_MALE;

    private static <T extends ChangedEntity> RegistryObject<TransfurVariant<T>> register(String name, TransfurVariant.Builder<T> builder) {
        return REGISTRY.register(name, builder::build);
    }

    public static class Gendered {
        private static final List<GenderedPair<?, ?>> PAIRS = new ArrayList<>();

        public static final GenderedPair<DarkLatexWolfMale, DarkLatexWolfFemale> DARK_LATEX_WOLVES = registerPair(DARK_LATEX_WOLF_MALE, DARK_LATEX_WOLF_FEMALE);

        public static <M extends ChangedEntity, F extends ChangedEntity> GenderedPair<M, F> registerPair(Supplier<? extends TransfurVariant<M>> maleVariant, Supplier<? extends TransfurVariant<F>> femaleVariant) {
            var pair = new GenderedPair<>(maleVariant, femaleVariant);
            PAIRS.add(pair);
            return pair;
        }

        public static Stream<GenderedPair<?, ?>> getPairs() {
            return PAIRS.stream();
        }

        public static Optional<TransfurVariant<?>> getOpposite(TransfurVariant<?> variant) {
            return getPairs().<TransfurVariant<?>>mapMulti((pair, consumer) -> {
                if (pair.getMaleVariant() == variant)
                    consumer.accept(pair.getFemaleVariant());
                else if (pair.getFemaleVariant() == variant)
                    consumer.accept(pair.getMaleVariant());
            }).findAny();
        }

        public static boolean hasOpposite(TransfurVariant<?> variant) {
            return getPairs().anyMatch(pair -> pair.contains(variant));
        }
    }
}
