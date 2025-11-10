package net.ltxprogrammer.changed.entity.animation;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class AnimationEvent<T extends AnimationParameters> extends ForgeRegistryEntry<AnimationEvent<?>> {
    private final Codec<T> codec;

    public static final Codec<AnimationParameters> NO_PARAMETERS = Codec.unit(() -> AnimationParameters.EMPTY);

    public AnimationEvent(Codec<T> codec) {
        this.codec = codec;
    }

    public Codec<T> getCodec() {
        return this.codec;
    }
}
