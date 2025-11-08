package net.ltxprogrammer.changed.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.ltxprogrammer.changed.ability.AbstractAbility;
import net.ltxprogrammer.changed.init.ChangedAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    @Final @Shadow private Minecraft minecraft;

    protected ClientLevelMixin(WritableLevelData data, ResourceKey<Level> key, Holder<DimensionType> dimension, Supplier<ProfilerFiller> profilerFillerSupplier, boolean p_204153_, boolean p_204154_, long p_204155_) {
        super(data, key, dimension, profilerFillerSupplier, p_204153_, p_204154_, p_204155_);
    }

    @WrapMethod(method = "tickNonPassenger")
    private void ensureSetNoCulling(Entity entity, Operation<Void> original) {
        boolean originalNoCullState = entity.noCulling;

        if (entity instanceof LivingEntity livingEntity && AbstractAbility.getAbilityInstanceSafe(livingEntity, ChangedAbilities.GRAB_ENTITY_ABILITY.get())
                .map(ability -> ability.grabbedEntity == this.minecraft.getCameraEntity()).orElse(false)) {
            entity.noCulling = true;
        }

        else if (this.minecraft.getCameraEntity() instanceof LivingEntity livingEntity && AbstractAbility.getAbilityInstanceSafe(livingEntity, ChangedAbilities.GRAB_ENTITY_ABILITY.get())
                .map(ability -> ability.grabbedEntity == entity).orElse(false)) {
            entity.noCulling = true;
        }

        original.call(entity);

        entity.noCulling = originalNoCullState;
    }
}
