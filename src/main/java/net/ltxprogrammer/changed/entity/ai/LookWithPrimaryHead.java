package net.ltxprogrammer.changed.entity.ai;

import net.ltxprogrammer.changed.entity.api.DoubleHeadedEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class LookWithPrimaryHead<T extends Mob & DoubleHeadedEntity> extends Goal {
    private final T mob;
    private int lookTime;
    protected final float probability;

    public LookWithPrimaryHead(T mob) {
        this(mob, 0.02f);
    }

    public LookWithPrimaryHead(T mob, float probability) {
        this.mob = mob;
        this.probability = probability;
    }

    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canContinueToUse() {
        return this.lookTime > 0;
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    public void tick() {
        mob.setYHeadRot(Mth.rotLerp(0.5f, mob.getYHeadRot(), mob.getHead2YRot()));
        mob.setXRot(Mth.rotLerp(0.5f, mob.getXRot(), mob.getHead2XRot()));
        --this.lookTime;
    }
}