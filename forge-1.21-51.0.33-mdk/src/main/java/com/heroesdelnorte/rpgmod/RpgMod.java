package com.heroesdelnorte.rpgmod;

import com.heroesdelnorte.rpgmod.client.renderer.EvilMarioRenderer;
import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("rpgmod")
public class RpgMod {

    public RpgMod() {
        // 1. Obtenemos el bus de eventos de Forge (el sistema que carga los mods)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 2. Cargamos nuestro catálogo de entidades
        ModEntities.ENTITIES.register(modEventBus);

        // 3. Escuchamos el evento de creación de atributos para dárselos al jefe
        modEventBus.addListener(this::registerAttributes);

        // 4. NUEVO: Escuchamos el evento para registrar el aspecto visual (renderizador)
        modEventBus.addListener(this::registerEntityRenderers);
    }

    // 5. Método que vincula la vida y daño que creaste con la entidad EvilMario
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.EVIL_MARIO.get(), EvilMario.createAttributes().build());
    }

    private void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EVIL_MARIO.get(), EvilMarioRenderer::new);
    }
}