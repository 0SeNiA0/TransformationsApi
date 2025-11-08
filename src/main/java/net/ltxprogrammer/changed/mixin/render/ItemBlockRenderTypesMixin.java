package net.ltxprogrammer.changed.mixin.render;

import net.ltxprogrammer.changed.client.ChangedClient;
import net.ltxprogrammer.changed.client.ChangedShaders;
import net.ltxprogrammer.changed.init.ChangedTags;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemBlockRenderTypes.class, remap = false, priority = 500)
public abstract class ItemBlockRenderTypesMixin {
    @Unique
    private static boolean recurseFlag = false;

    private static boolean checkResonantBlockLayers(BlockState state, RenderType type, CallbackInfoReturnable<Boolean> callback) {
        if (!ChangedClient.shouldRenderingWaveVision())
            return false;
        if (!state.is(ChangedTags.Blocks.CRYSTALLINE) || recurseFlag)
            return false;

        if (type == RenderType.solid() || type == RenderType.cutoutMipped() || type == RenderType.cutout() || type == RenderType.translucent()) {
            callback.setReturnValue(false);
            return true;
        }

        recurseFlag = true;
        if (type == ChangedShaders.waveVisionResonantSolidFixed())
            callback.setReturnValue(ItemBlockRenderTypes.canRenderInLayer(state, RenderType.solid()));
        else if (type == ChangedShaders.waveVisionResonantCutoutMippedFixed())
            callback.setReturnValue(ItemBlockRenderTypes.canRenderInLayer(state, RenderType.cutoutMipped()));
        else if (type == ChangedShaders.waveVisionResonantCutoutFixed())
            callback.setReturnValue(
                    ItemBlockRenderTypes.canRenderInLayer(state, RenderType.cutout()) ||
                            ItemBlockRenderTypes.canRenderInLayer(state, RenderType.translucent()));
        recurseFlag = false;
        return callback.isCancelled();
    }

    @Inject(method = "canRenderInLayer(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/renderer/RenderType;)Z", at = @At("HEAD"), cancellable = true)
    private static void canCoveredBlockRenderInLayer(BlockState state, RenderType type, CallbackInfoReturnable<Boolean> callback) {
        checkResonantBlockLayers(state, type, callback);
    }
}
