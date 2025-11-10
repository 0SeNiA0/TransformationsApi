package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.entity.api.ChangedEntity;
import net.ltxprogrammer.changed.entity.api.TamableLatexEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class LatexOwnerHurtTargetGoal<T extends ChangedEntity & TamableLatexEntity> extends TargetGoal {
    private final T tameAnimal;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public LatexOwnerHurtTargetGoal(T p_26114_) {
        super(p_26114_, false);
        this.tameAnimal = p_26114_;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public boolean canUse() {
        if (this.tameAnimal.isTame()) {
            if (!(this.tameAnimal.getOwner() instanceof LivingEntity livingentity)) {
                return false;
            } else {
                this.ownerLastHurt = livingentity.getLastHurtMob();
                int i = livingentity.getLastHurtMobTimestamp();
                return i != this.timestamp && this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.tameAnimal.wantsToAttack(this.ownerLastHurt, livingentity);
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        if (this.tameAnimal.getOwner() instanceof LivingEntity livingentity) {
            this.timestamp = livingentity.getLastHurtMobTimestamp();
        }

        super.start();
    }
}