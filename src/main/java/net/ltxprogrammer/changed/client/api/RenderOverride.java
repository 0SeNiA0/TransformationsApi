package net.ltxprogrammer.changed.client.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.ltxprogrammer.changed.transform.TransfurVariantInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public abstract class RenderOverride {
    public interface Override {
        boolean requireVariant();
        boolean wantToOverride(Player player, TransfurVariantInstance<?> variant);
        void render(Player player, @Nullable TransfurVariantInstance<?> variant, PoseStack stack, MultiBufferSource buffer, int packedLight, float partialTick);
    }

    private static int EXPECTED_SIZE = 0;
    private static final List<Function<EntityModelSet, Override>> OVERRIDES_PRE_INIT = new ArrayList<>();
    private static List<Override> OVERRIDES = new ArrayList<>();
    
    public static void registerOverride(Function<EntityModelSet, Override> fn) {
        OVERRIDES_PRE_INIT.add(fn);
        EXPECTED_SIZE = OVERRIDES_PRE_INIT.hashCode();
    }

    protected static void checkOverrides() {
        if (EXPECTED_SIZE == OVERRIDES_PRE_INIT.size())
            return;

        var modelSet = Minecraft.getInstance().getEntityModels();
        OVERRIDES = OVERRIDES_PRE_INIT.stream().map(fn -> fn.apply(modelSet)).collect(Collectors.toList());
        EXPECTED_SIZE = OVERRIDES_PRE_INIT.size();
    }
    
    public static boolean renderOverrides(Player player, @Nullable TransfurVariantInstance<?> variant, PoseStack stack, MultiBufferSource buffer, int packedLight, float partialTick) {
        checkOverrides();

        for (var override : OVERRIDES) {
            if (override.requireVariant() == (variant == null))
                continue;
            if (!override.wantToOverride(player, variant))
                continue;
            
            override.render(player, variant, stack, buffer, packedLight, partialTick);
            return true;
        }
        
        return false;
    }
}
