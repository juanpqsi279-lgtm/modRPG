package com.heroesdelnorte.rpgmod.client;

import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.client.renderer.EvilMarioRenderer;
import com.heroesdelnorte.rpgmod.client.renderer.NpcRenderer;
import com.heroesdelnorte.rpgmod.client.renderer.EvilMinionRenderer; // Asegúrate de tener este también
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// La anotación @Mod.EventBusSubscriber asegura que Forge lea este archivo al arrancar el juego (solo en el Cliente)
@Mod.EventBusSubscriber(modid = "rpgmod", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Aquí conectamos cada Entidad con su Renderizador
        event.registerEntityRenderer(ModEntities.EVIL_MARIO.get(), EvilMarioRenderer::new);
        event.registerEntityRenderer(ModEntities.NPC_BASE.get(), NpcRenderer::new);

        // Si ya habías creado el EvilMinionRenderer, regístralo aquí también:
        event.registerEntityRenderer(ModEntities.EVIL_MINION.get(), EvilMinionRenderer::new);
    }
}