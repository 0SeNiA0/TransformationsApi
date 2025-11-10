package net.ltxprogrammer.changed.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.animations.AnimationAssociations;
import net.ltxprogrammer.changed.client.animations.AnimationDefinitions;
import net.ltxprogrammer.changed.client.renderer.layers.FirstPersonLayer;
import net.ltxprogrammer.changed.transform.ProcessTransform;
import net.ltxprogrammer.changed.transform.TransfurVariantInstance;
import net.ltxprogrammer.changed.transform.VisionType;
import net.ltxprogrammer.changed.util.Cacheable;
import net.ltxprogrammer.changed.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ChangedClient {
    private static final Minecraft minecraft = Minecraft.getInstance();
    public static final Cacheable<AbilityColors> abilityColors = Cacheable.of(AbilityColors::createDefault);
    public static final Cacheable<AbilityRenderer> abilityRenderer = Cacheable.of(() -> new AbilityRenderer(minecraft.textureManager, minecraft.getModelManager(), abilityColors.getOrThrow()));

    public static void registerEventListeners() {
        Changed.addEventListener(ChangedClient::afterRenderStage);
    }

    public static void registerReloadListeners(Consumer<PreparableReloadListener> resourceManager) {
        resourceManager.accept(abilityRenderer.getOrThrow());
        resourceManager.accept(AnimationDefinitions.INSTANCE);
        resourceManager.accept(AnimationAssociations.INSTANCE);
    }

    public static void afterRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            FirstPersonLayer.renderFirstPersonLayersOnFace(event.getPoseStack(), event.getCamera(), event.getPartialTick());
        }
    }

    private static List<Consumer<VertexConsumer>> TRANSLUCENT_CONSUMERS = new ArrayList<>();

    public static void runRecordedTranslucentRender(MultiBufferSource buffers, RenderType renderType) {
        final VertexConsumer buffer = buffers.getBuffer(renderType);
        TRANSLUCENT_CONSUMERS.forEach(consumer -> consumer.accept(buffer));
        TRANSLUCENT_CONSUMERS.clear();
    }

    public static void recordTranslucentRender(MultiBufferSource buffers, RenderType renderType, Consumer<VertexConsumer> consumer) {
        if (renderType == RenderType.translucent()) {
            TRANSLUCENT_CONSUMERS.add(consumer);
        } else {
            consumer.accept(buffers.getBuffer(renderType));
        }
    }

    public enum WaveVisionRenderPhase {
        TERRAIN,
        ENTITIES,
        BLOCK_ENTITIES
    }

    private static WaveVisionRenderPhase phase = WaveVisionRenderPhase.TERRAIN;

    public static WaveVisionRenderPhase getWaveRenderPhase() {
        return phase;
    }

    public static void setWaveRenderPhase(WaveVisionRenderPhase phase) {
        ChangedClient.phase = phase;
    }

    public static boolean shouldRenderingWaveVision() {
        return ProcessTransform.getPlayerTransfurVariantSafe(EntityUtil.playerOrNull(minecraft.cameraEntity))
                .map(variant -> variant.visionType == VisionType.WAVE_VISION)
                .orElse(false);
    }

    private static boolean renderingWaveVision = false;
    private static float waveEffect = 0.0f;
    private static Vector3f waveResonance = Vector3f.ZERO;
    public static float setupWaveVisionEffect(float partialTicks) {
        float effect = ProcessTransform.getPlayerTransfurVariantSafe(EntityUtil.playerOrNull(minecraft.cameraEntity))
                .filter(variant -> variant.visionType == VisionType.WAVE_VISION)
                .map(TransfurVariantInstance::getTicksInWaveVision)
                .map(ticks -> ticks + partialTicks).orElse(0.0f);

        waveEffect = effect * 0.5f;
        return waveEffect;
    }

    public static void setRenderingWaveVision(boolean renderingWaveVision) {
        ChangedClient.renderingWaveVision = renderingWaveVision;
    }

    public static boolean isRenderingWaveVision() {
        return ChangedClient.renderingWaveVision;
    }

    public static float getWaveEffect() {
        return waveEffect;
    }

    public static void setWaveResonance(Vector3f resonance) {
        ChangedClient.waveResonance = resonance;
    }

    public static void resetWaveResonance() {
        ChangedClient.waveResonance = Vector3f.ZERO;
    }

    public static Vector3f getWaveResonance() {
        return waveResonance;
    }
}
