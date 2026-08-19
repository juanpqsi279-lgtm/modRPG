package com.heroesdelnorte.rpgmod.client.renderer;

import com.heroesdelnorte.rpgmod.entity.EvilMinion;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EvilMinionRenderer extends HumanoidMobRenderer<EvilMinion, HumanoidModel<EvilMinion>> {

    // Ruta donde tu compañero pondrá la textura evil_minion.png
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/evil_minion.png");

    public EvilMinionRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EvilMinion entity) {
        return TEXTURE;
    }
}