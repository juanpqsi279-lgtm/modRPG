package com.heroesdelnorte.rpgmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CharacterSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public CharacterSkinLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (player.isInvisible()) return;

        // Leemos la clase seleccionada guardada en los datos del jugador
        String characterClass = player.getPersistentData().getString("RpgModClass");
        if (characterClass.isEmpty()) return;

        // Asignamos la textura correspondiente
        ResourceLocation skinTexture = switch (characterClass) {
            case "Uriel" -> ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/player/uriel.png");
            case "ChatGPT" -> ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/player/chatgpt.png");
            case "Josepe" -> ResourceLocation.fromNamespaceAndPath("rpgmod", "textures/entity/player/josepe.png");
            default -> null;
        };

        if (skinTexture != null) {
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(skinTexture));
            // Dibuja el cuerpo y la ropa del personaje con la skin elegida
            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}