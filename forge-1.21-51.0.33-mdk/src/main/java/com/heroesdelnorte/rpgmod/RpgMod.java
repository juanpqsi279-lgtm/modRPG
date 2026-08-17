package com.heroesdelnorte.rpgmod;

import com.heroesdelnorte.rpgmod.client.renderer.EvilMarioRenderer;
import com.heroesdelnorte.rpgmod.client.renderer.NpcRenderer;
import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("rpgmod")
public class RpgMod {

    public RpgMod() {
        // 1. Obtenemos el bus de eventos de Forge (el sistema que carga los mods)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 2. Cargamos nuestro catálogo de entidades e ítems
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.register(modEventBus);

        // 3. REGISTRAMOS NUESTRA RED (Soluciona el crasheo de los inventarios)
        ModMessages.register();

        // 4. Escuchamos los eventos visuales y de atributos
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerEntityRenderers);

        // 5. Registramos esta clase principal en el bus general de Forge
        MinecraftForge.EVENT_BUS.register(this);
    }

    // Método que vincula la vida y daño
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.EVIL_MARIO.get(), EvilMario.createAttributes().build());
        event.put(ModEntities.NPC_BASE.get(), NpcBase.createAttributes().build());
    }

    // Método para registrar el aspecto visual (renderizador)
    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EVIL_MARIO.get(), EvilMarioRenderer::new);
        event.registerEntityRenderer(ModEntities.NPC_BASE.get(), NpcRenderer::new);
    }
}