package com.heroesdelnorte.rpgmod.client.renderer;

import com.heroesdelnorte.rpgmod.entity.EvilMario;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EvilMarioRenderer extends HumanoidMobRenderer<EvilMario, HumanoidModel<EvilMario>> {

    public EvilMarioRenderer(EntityRendererProvider.Context context) {
        // Usamos el modelo estándar de humanoide y una textura por defecto de Steve por ahora
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(EvilMario entity) {
        // Apunta al archivo exacto que guardaste en la carpeta entity
        return ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/evil_mario.png");
    }
}