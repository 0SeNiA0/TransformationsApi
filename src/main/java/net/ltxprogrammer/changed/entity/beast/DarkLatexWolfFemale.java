package net.ltxprogrammer.changed.entity.beast;

import net.ltxprogrammer.changed.transform.Gender;
import net.ltxprogrammer.changed.transform.TransfurMode;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class DarkLatexWolfFemale extends AbstractDarkLatexWolf {
    public DarkLatexWolfFemale(EntityType<? extends DarkLatexWolfFemale> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public TransfurMode getTransfurMode() {
        return TransfurMode.ABSORPTION;
    }

    @Override
    public Gender getGender() {
        return Gender.FEMALE;
    }
}
