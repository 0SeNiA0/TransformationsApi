package net.ltxprogrammer.changed.entity.api;

import net.ltxprogrammer.changed.data.AccessorySlots;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public interface LivingEntityDataExtension {
    int getNoControlTicks();
    void setNoControlTicks(int ticks);

    @Nullable
    LivingEntity getGrabbedBy();
    void setGrabbedBy(@Nullable LivingEntity holder);

    void do_hurtCurrentlyUsedShield(float blocked);
    void do_blockUsingShield(LivingEntity attacker);

    Optional<AccessorySlots> getAccessorySlots();
}
