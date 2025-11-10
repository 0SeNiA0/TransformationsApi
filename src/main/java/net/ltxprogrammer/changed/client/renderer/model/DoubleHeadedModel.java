package net.ltxprogrammer.changed.client.renderer.model;

import net.ltxprogrammer.changed.entity.api.ChangedEntity;
import net.minecraft.client.model.geom.ModelPart;

public interface DoubleHeadedModel<T extends ChangedEntity> {
    ModelPart getHead();
    ModelPart getOtherHead();
}
