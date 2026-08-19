package com.heroesdelnorte.rpgmod;

import com.heroesdelnorte.rpgmod.client.renderer.EvilMarioRenderer;
import com.heroesdelnorte.rpgmod.client.renderer.EvilMinionRenderer;
import com.heroesdelnorte.rpgmod.client.renderer.NpcRenderer;
import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.entity.EvilMinion;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.registry.ModBlocks;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("rpgmod")
public class RpgMod {

    public RpgMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 1. Catálogos de Registro
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // ¡ESTA ES LA LÍNEA QUE REVIVE EL MENÚ DE LA LETRA 'R'!
        ModMessages.register();

        // 2. Eventos del ciclo de vida del mod
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerEntityRenderers);

        // 3. Conexión del inventario creativo
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    // Método para inyectar los huevos en el inventario creativo (Pestaña Huevos)
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.EVIL_MINION_SPAWN_EGG);
            event.accept(ModItems.NPC_SPAWN_EGG);
        }
    }

    // Registro de vida, daño y atributos base
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.EVIL_MARIO.get(), EvilMario.createAttributes().build());
        event.put(ModEntities.NPC_BASE.get(), NpcBase.createAttributes().build());
        event.put(ModEntities.EVIL_MINION.get(), EvilMinion.createAttributes().build());
    }

    // Registro visual de modelos y texturas
    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EVIL_MARIO.get(), EvilMarioRenderer::new);
        event.registerEntityRenderer(ModEntities.NPC_BASE.get(), NpcRenderer::new);
        event.registerEntityRenderer(ModEntities.EVIL_MINION.get(), EvilMinionRenderer::new);
    }
}