package com.heroesdelnorte.rpgmod;

import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.entity.EvilMinion;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.registry.ModBlocks;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
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

        // 1. Catálogos de Registro (ModBlocks antes de ModItems para registrar el BlockItem del portal)
        ModEntities.ENTITIES.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        // 2. Red cliente-servidor
        ModMessages.register();

        // 3. Eventos del ciclo de vida del mod
        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    // Método para inyectar los ítems del mod en las pestañas del inventario creativo
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.EVIL_MINION_SPAWN_EGG);
            event.accept(ModItems.NPC_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.WRENCH);
            event.accept(ModItems.WHIP);
            event.accept(ModItems.JOSEPE_AXE);
            event.accept(ModItems.GLITCH_SWORD);
            event.accept(ModItems.DROP_TABLE_SWORD);
            event.accept(ModItems.FIREWALL_HELMET);
            event.accept(ModItems.FIREWALL_CHESTPLATE);
            event.accept(ModItems.FIREWALL_LEGGINGS);
            event.accept(ModItems.FIREWALL_BOOTS);
            event.accept(ModItems.QUANTUM_HELMET);
            event.accept(ModItems.QUANTUM_CHESTPLATE);
            event.accept(ModItems.QUANTUM_LEGGINGS);
            event.accept(ModItems.QUANTUM_BOOTS);
            event.accept(ModItems.ADMIN_HELMET);
            event.accept(ModItems.ADMIN_CHESTPLATE);
            event.accept(ModItems.ADMIN_LEGGINGS);
            event.accept(ModItems.ADMIN_BOOTS);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.PORTAL_BLOCK_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BOSS_FRAGMENT);
            event.accept(ModItems.BOSS_KEY);
            event.accept(ModItems.CORRUPTED_BIT);
            event.accept(ModItems.SYSTEM_LOG);
            event.accept(ModItems.STRUCTURE_MAP);
        }
    }

    // Registro de vida, daño y atributos base
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.EVIL_MARIO.get(), EvilMario.createAttributes().build());
        event.put(ModEntities.NPC_BASE.get(), NpcBase.createAttributes().build());
        event.put(ModEntities.EVIL_MINION.get(), EvilMinion.createAttributes().build());
    }
}