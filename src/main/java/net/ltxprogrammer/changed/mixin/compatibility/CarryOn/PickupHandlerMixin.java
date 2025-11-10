package net.ltxprogrammer.changed.mixin.compatibility.CarryOn;

import net.ltxprogrammer.changed.entity.api.ChangedEntity;
import net.ltxprogrammer.changed.extension.RequiredMods;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.common.config.Configs;
import tschipp.carryon.common.handler.PickupHandler;

@Mixin(value = PickupHandler.class, remap = false)
@RequiredMods("carryon")
public class PickupHandlerMixin {
    @Inject(method = "canPlayerPickUpEntity", at = @At("HEAD"), cancellable = true)
    private static void handleChangedEntities(ServerPlayer player, Entity toPickUp, CallbackInfoReturnable<Boolean> cir) {
        if (!(toPickUp instanceof ChangedEntity)) return;
        if (Configs.Settings.pickupHostileMobs.get()) return;
        cir.setReturnValue(false);
    }
}
