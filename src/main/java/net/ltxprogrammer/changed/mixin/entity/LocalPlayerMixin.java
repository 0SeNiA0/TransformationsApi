package net.ltxprogrammer.changed.mixin.entity;


import com.mojang.authlib.GameProfile;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.NullInput;
import net.ltxprogrammer.changed.client.api.LocalPlayerAccessor;
import net.ltxprogrammer.changed.entity.api.LivingEntityDataExtension;
import net.ltxprogrammer.changed.entity.api.PlayerDataExtension;
import net.ltxprogrammer.changed.transform.ProcessTransform;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements PlayerDataExtension, LivingEntityDataExtension, LocalPlayerAccessor {
    public LocalPlayerMixin(ClientLevel p_108548_, GameProfile p_108549_) {
        super(p_108548_, p_108549_);
    }

    @Shadow public Input input;
    @Shadow @Final protected Minecraft minecraft;

    @Shadow private boolean flashOnSetHealth;

    @Shadow private boolean handsBusy;

    @Override
    public void setHandsBusy(boolean busy) {
        this.handsBusy = busy;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void applyBasicPlayerInfo(Minecraft mc, ClientLevel level, ClientPacketListener packetListener, StatsCounter stats, ClientRecipeBook recipeBook, boolean p_108626_, boolean p_108627_, CallbackInfo ci) {
        this.setBasicPlayerInfo(Changed.config.client.basicPlayerInfo);
    }

    @Inject(method = "getWaterVision", at = @At("RETURN"), cancellable = true)
    private void getWaterVision(CallbackInfoReturnable<Float> callback) {
        ProcessTransform.ifPlayerTransfurred(this, variant -> {
            if (!variant.getParent().getBreatheMode().canBreatheWater())
                return;
            if (!this.isEyeInFluid(FluidTags.WATER))
                return;
            for (var level : Thread.currentThread().getStackTrace()) {
                if (level.toString().contains("LightTexture")) // Light texture breaks when returning > 1.0F
                    return;
            }

            callback.setReturnValue(callback.getReturnValue() * 4.0F);
        });
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void aiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (!player.level.isClientSide) return;

        ProcessTransform.ifPlayerTransfurred(player, variant -> {
            if (player.getAttributeBaseValue(ForgeMod.SWIM_SPEED.get()) >= 1.1F && variant.getEntityShape().isLegless() && player.isUnderWater())
                player.setSprinting(true);
        });
    }

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    public void isMovingSlowly(CallbackInfoReturnable<Boolean> callback) {
        ProcessTransform.ifPlayerTransfurred(this, variant -> {
            if (variant.getChangedEntity() != null)
                callback.setReturnValue(variant.getChangedEntity().isMovingSlowly());
        });
    }

    @Unique
    private Input inputCopy = null;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        boolean isNullInput = input instanceof NullInput;
        if (this.getNoControlTicks() > 0) {
            if (!isNullInput) {
                inputCopy = input;
                input = new NullInput();
            }
        } else if (isNullInput) {
            input = inputCopy;
            inputCopy = null;
        }
    }

    @Inject(method = "hurtTo", at = @At("HEAD"))
    public void disableFlashOnTf(float health, CallbackInfo ci) {
        ProcessTransform.ifPlayerTransfurred(EntityUtil.playerOrNull(this), (player, variant) -> {
            if (variant.isTransfurring())
                this.flashOnSetHealth = false;
        });
    }
}
