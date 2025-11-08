package net.ltxprogrammer.changed.client;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.gui.AbilityOverlay;
import net.ltxprogrammer.changed.client.gui.GrabOverlay;
import net.ltxprogrammer.changed.client.gui.TransfurProgressOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChangedOverlays {
    protected static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");

    public static final IIngameOverlay DANGER_ELEMENT = OverlayRegistry.registerOverlayTop(Changed.modResourceStr("danger"), (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        TransfurProgressOverlay.renderDangerOverlay(gui, poseStack, partialTick, screenWidth, screenHeight);
    });
    public static final IIngameOverlay ABILITY_ELEMENT = OverlayRegistry.registerOverlayAbove(DANGER_ELEMENT, Changed.modResourceStr("ability"), (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
        gui.setupOverlayRenderState(true, false);
        AbilityOverlay.renderSelectedAbility(gui, poseStack, partialTick, screenWidth, screenHeight);
    });
    public static final IIngameOverlay GRABBED_ELEMENT = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.EXPERIENCE_BAR_ELEMENT, Changed.modResourceStr("grabbed"), GrabOverlay::renderProgressBars);
}
