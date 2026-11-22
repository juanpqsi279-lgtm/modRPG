package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.client.renderer.CharacterSkinLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerSkinHandler {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getPlayerSkin(skin);
            if (renderer instanceof PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new CharacterSkinLayer(playerRenderer));
            }
        }
    }
}