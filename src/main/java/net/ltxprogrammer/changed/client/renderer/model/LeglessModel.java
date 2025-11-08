package net.ltxprogrammer.changed.client.renderer.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface LeglessModel {
    ModelPart getAbdomen();

    public static boolean shouldLeglessSit(LivingEntity entity) {
        return false;
    }
}
