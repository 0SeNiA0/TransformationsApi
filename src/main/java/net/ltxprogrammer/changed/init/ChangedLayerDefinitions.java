package net.ltxprogrammer.changed.init;

import net.ltxprogrammer.changed.client.renderer.layers.CustomEyesLayer;
import net.ltxprogrammer.changed.client.renderer.layers.TransfurCapeLayer;
import net.ltxprogrammer.changed.client.renderer.model.DarkLatexWolfFemaleModel;
import net.ltxprogrammer.changed.client.renderer.model.DarkLatexWolfMaleModel;
import net.ltxprogrammer.changed.client.renderer.model.DarkLatexWolfPupModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexFemaleWolfModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorLatexMaleWolfModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorNoTailModel;
import net.ltxprogrammer.changed.client.renderer.model.armor.ArmorNoneModel;
import net.ltxprogrammer.changed.client.renderer.model.hair.HairRemodel;
import net.ltxprogrammer.changed.client.tfanimations.TransfurHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChangedLayerDefinitions {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CustomEyesLayer.HEAD, CustomEyesLayer::createHead);
        event.registerLayerDefinition(TransfurCapeLayer.LAYER_LOCATION, TransfurCapeLayer::createCape);
        event.registerLayerDefinition(TransfurCapeLayer.LAYER_LOCATION_SHORT, TransfurCapeLayer::createShortCape);

        event.registerLayerDefinition(TransfurHelper.TRANSFUR_HELPER, TransfurHelper::createBodyLayer);

        event.registerLayerDefinition(DarkLatexWolfFemaleModel.LAYER_LOCATION, DarkLatexWolfFemaleModel::createBodyLayer);
        event.registerLayerDefinition(DarkLatexWolfMaleModel.LAYER_LOCATION, DarkLatexWolfMaleModel::createBodyLayer);
        event.registerLayerDefinition(DarkLatexWolfPupModel.LAYER_LOCATION, DarkLatexWolfPupModel::createBodyLayer);

        ArmorNoneModel.MODEL_SET.registerDefinitions(event::registerLayerDefinition);
        ArmorNoTailModel.MODEL_SET.registerDefinitions(event::registerLayerDefinition);

        // v --- ARMOR / ENTITY --- ^

        ArmorLatexMaleWolfModel.MODEL_SET.registerDefinitions(event::registerLayerDefinition);
        ArmorLatexFemaleWolfModel.MODEL_SET.registerDefinitions(event::registerLayerDefinition);

        // TODO maybe revisit custom hair in the future
        /*event.registerLayerDefinition(HairRemodel.RIG_UPPER_LOCATION, HairRemodel::createUpperHair);
        event.registerLayerDefinition(HairRemodel.RIG_LOWER_LOCATION, HairRemodel::createLowerHair);*/
    }
}
