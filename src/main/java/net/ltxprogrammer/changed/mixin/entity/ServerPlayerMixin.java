package net.ltxprogrammer.changed.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.data.AccessorySlots;
import net.ltxprogrammer.changed.entity.api.PlayerDataExtension;
import net.ltxprogrammer.changed.init.*;
import net.ltxprogrammer.changed.network.packet.MountTransfurPacket;
import net.ltxprogrammer.changed.transform.ProcessTransform;
import net.ltxprogrammer.changed.transform.TransfurVariant;
import net.ltxprogrammer.changed.transform.TransfurVariantInstance;
import net.ltxprogrammer.changed.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PlayerDataExtension {
    @Shadow private int spawnInvulnerableTime;

    public ServerPlayerMixin(Level p_36114_, BlockPos p_36115_, float p_36116_, GameProfile p_36117_) {
        super(p_36114_, p_36115_, p_36116_, p_36117_);
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    public void restoreFrom(ServerPlayer player, boolean restore, CallbackInfo callbackInfo) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (player.level.getGameRules().getBoolean(ChangedGameRules.RULE_KEEP_FORM) || restore) {
            ProcessTransform.ifPlayerTransfurred(player, oldVariant -> {
                if (!oldVariant.willSurviveTransfur)
                    return;
                if (!restore && oldVariant.getParent().is(ChangedTags.TransfurVariants.TEMPORARY_ONLY))
                    return; // Exception to keepForm gamerule

                var newVariant = ProcessTransform.setPlayerTransfurVariant(self, oldVariant.getParent(), oldVariant.transfurContext, oldVariant.transfurProgression);
                if (newVariant == null)
                    return;
                newVariant.load(oldVariant.save());
                newVariant.handleRespawn();
            });
        }

        this.getAccessorySlots().ifPresent(slots -> slots.replaceWith(AccessorySlots.getForEntity(player).orElse(null)));
    }

    @WrapMethod(method = "hurt")
    private boolean ignoreInvulnForTFKill(DamageSource source, float damage, Operation<Boolean> original) {
        int oldSpawnInvulnerableTime = this.spawnInvulnerableTime;
        var damageSourceName = source.msgId;

        boolean wrapInvuln;
        if (this.spawnInvulnerableTime > 0 && this.getTransfurVariant() != null && (ChangedDamageSources.TRANSFUR_NAME.equals(damageSourceName)
                || ChangedDamageSources.ABSORB_NAME.equals(damageSourceName))) {
            this.spawnInvulnerableTime = 0;
            wrapInvuln = true;
        }

        else {
            wrapInvuln = false;
        }

        var result = original.call(source, damage);

        if (wrapInvuln) {
            this.spawnInvulnerableTime = oldSpawnInvulnerableTime;
        }

        return result;
    }

    @Unique
    private void readTransfurVariant(CompoundTag tag) {
        if (tag.contains("TransfurVariant")) {
            ResourceLocation variantId = TagUtil.getResourceLocation(tag, "TransfurVariant");
            TransfurVariant<?> variant = ChangedRegistry.TRANSFUR_VARIANT.get().getValue(variantId);
            if (variant == null) {
                Changed.LOGGER.warn("Missing transfur variant registry entry for {}, falling back", variantId);
                variant = ChangedTransfurVariants.FALLBACK_VARIANT.get();
            }
            final TransfurVariantInstance<?> variantInstance = ProcessTransform.setPlayerTransfurVariant(this, variant, null, 1.0f, false,
                    entity -> {
                        if (tag.contains("Leash", 10))
                            entity.setLeashInfoTag(tag.getCompound("Leash"));
                    });

            if (variantInstance == null) {
                Changed.LOGGER.warn("Instanced transfur variant is null");
                return;
            }

            if (tag.contains("TransfurVariantData"))
                variantInstance.load(tag.getCompound("TransfurVariantData"));
            else {
                if (tag.contains("TransfurVariantAge"))
                    variantInstance.ageAsVariant = tag.getInt("TransfurVariantAge");
                if (tag.contains("TransfurAbilities"))
                    variantInstance.loadAbilities(tag.getCompound("TransfurAbilities"));
            }
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    protected void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("TransfurProgress")) {
            if (tag.get("TransfurProgress") instanceof IntTag intTag) { // Adapt to old progress saving method
                ProcessTransform.setPlayerTransfurProgress(this, (float)intTag.getAsInt() * 0.001f);
            } else if (tag.get("TransfurProgress") instanceof FloatTag floatTag) {
                ProcessTransform.setPlayerTransfurProgress(this, floatTag.getAsFloat());
            }
        }

        readTransfurVariant(tag);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    protected void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putFloat("TransfurProgress", ProcessTransform.getPlayerTransfurProgress(this));
        ProcessTransform.ifPlayerTransfurred(this, variant -> {
            TagUtil.putResourceLocation(tag, "TransfurVariant", variant.getFormId());
            tag.put("TransfurVariantData", variant.save());

            var entity = variant.getChangedEntity();

            if (entity.getLeashHolder() != null) {
                CompoundTag compoundtag2 = new CompoundTag();
                if (entity.getLeashHolder() instanceof LivingEntity) {
                    UUID uuid = entity.getLeashHolder().getUUID();
                    compoundtag2.putUUID("UUID", uuid);
                } else if (entity.getLeashHolder() instanceof HangingEntity) {
                    BlockPos blockpos = ((HangingEntity) entity.getLeashHolder()).getPos();
                    compoundtag2.putInt("X", blockpos.getX());
                    compoundtag2.putInt("Y", blockpos.getY());
                    compoundtag2.putInt("Z", blockpos.getZ());
                }

                tag.put("Leash", compoundtag2);
            } else if (entity.getLeashInfoTag() != null) {
                tag.put("Leash", entity.getLeashInfoTag().copy());
            }
        });
    }

    @Inject(method = "stopRiding", at = @At("HEAD"))
    public void stopRiding(CallbackInfo callbackInfo) {
        if (this.getVehicle() instanceof Player)
            Changed.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new MountTransfurPacket(getUUID(), getUUID()));
    }
}
