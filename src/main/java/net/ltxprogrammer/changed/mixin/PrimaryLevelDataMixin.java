package net.ltxprogrammer.changed.mixin;

import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin {

    // FORGE

    @Inject(method = "hasConfirmedExperimentalWarning", at = @At("RETURN"), cancellable = true, remap = false)
    public void hasConfirmedExperimentalWarning(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(true);
    }
}
