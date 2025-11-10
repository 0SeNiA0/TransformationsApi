package net.ltxprogrammer.changed.init;

import net.ltxprogrammer.changed.Changed;
import net.minecraft.world.entity.MobCategory;

public class ChangedMobCategories {
    public static final MobCategory CHANGED = MobCategory.create("CHANGED",
            Changed.modResourceStr("changed"), 10, false, false, 128);
}
