package net.ltxprogrammer.changed.ability;

import net.ltxprogrammer.changed.transform.ProcessTransform;
import net.ltxprogrammer.changed.transform.VisionType;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.Collections;

public class ToggleNightVisionAbility extends SimpleAbility {
    @Override
    public boolean canUse(IAbstractChangedEntity entity) {
        return true;
    }

    @Override
    public void startUsing(IAbstractChangedEntity entity) {
        super.startUsing(entity);

        ProcessTransform.ifPlayerTransfurred(EntityUtil.playerOrNull(entity.getEntity()), (player, variant) -> {
            variant.visionType = variant.visionType == VisionType.NORMAL ? VisionType.NIGHT_VISION : VisionType.NORMAL;
        });
    }

    @Override
    public UseType getUseType(IAbstractChangedEntity entity) {
        return UseType.INSTANT;
    }

    @Override
    public int getCoolDown(IAbstractChangedEntity entity) {
        return 60;
    }

    private static final Collection<Component> DESCRIPTION = Collections.singleton(new TranslatableComponent("ability.changed.toggle_night_vision.desc"));

    @Override
    public Collection<Component> getAbilityDescription(IAbstractChangedEntity entity) {
        return DESCRIPTION;
    }
}
