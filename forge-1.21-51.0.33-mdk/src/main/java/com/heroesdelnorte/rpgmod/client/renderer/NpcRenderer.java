package com.heroesdelnorte.rpgmod.client.renderer;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NpcRenderer extends MobRenderer<NpcBase, HumanoidModel<NpcBase>> {

    public NpcRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(NpcBase entity) {
        int skinId = entity.getSkinId();
        return ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/npc_" + skinId + ".png");
    }
}