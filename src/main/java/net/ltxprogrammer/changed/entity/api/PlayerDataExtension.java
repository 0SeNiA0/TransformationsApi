package net.ltxprogrammer.changed.entity.api;

import net.ltxprogrammer.changed.transform.BasicPlayerInfo;
import net.ltxprogrammer.changed.transform.TransfurVariantInstance;
import net.ltxprogrammer.changed.util.CameraUtil;

import javax.annotation.Nullable;

public interface PlayerDataExtension extends LivingEntityDataExtension {
    @Nullable
    TransfurVariantInstance<?> getTransfurVariant();
    void setTransfurVariant(@Nullable TransfurVariantInstance<?> variant);

    default boolean isTransfurred() {
        return getTransfurVariant() != null;
    }

    float getTransfurProgress();
    void setTransfurProgress(float progress);

    CameraUtil.TugData getTugData();
    void setTugData(CameraUtil.TugData data);

    int getPaleExposure();
    void setPaleExposure(int level);

    BasicPlayerInfo getBasicPlayerInfo();
    void setBasicPlayerInfo(BasicPlayerInfo basicPlayerInfo);
}
