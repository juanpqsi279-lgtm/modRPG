package com.heroesdelnorte.rpgmod.registry;

import com.heroesdelnorte.rpgmod.item.GlitchSwordItem;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.item.SystemLogItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.item.Tiers;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "rpgmod");

    // Armas de Clases (Ya sin el texto extra que causaba el error)
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench",
            () -> new RpgWeapon(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WHIP = ITEMS.register("whip",
            () -> new RpgWeapon(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> JOSEPE_AXE = ITEMS.register("josepe_axe",
            () -> new RpgWeapon(new Item.Properties().stacksTo(1)));

    // Materiales e Invocadores del Jefe
    public static final RegistryObject<Item> BOSS_FRAGMENT = ITEMS.register("boss_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BOSS_KEY = ITEMS.register("boss_key",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STRUCTURE_MAP = ITEMS.register("structure_map",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    // Huevo de invocación del Minion
    public static final RegistryObject<Item> EVIL_MINION_SPAWN_EGG = ITEMS.register("evil_minion_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.EVIL_MINION, 0x4A0000, 0x000000, new Item.Properties()));

    // Huevo de invocación del Aldeano
    public static final RegistryObject<Item> NPC_SPAWN_EGG = ITEMS.register("npc_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.NPC_BASE, 0x563C33, 0x388E3C, new Item.Properties()));

    public static final RegistryObject<Item> GLITCH_SWORD = ITEMS.register("glitch_sword",
            () -> new GlitchSwordItem(Tiers.NETHERITE, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> CORRUPTED_BIT = ITEMS.register("corrupted_bit",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SYSTEM_LOG = ITEMS.register("system_log",
            () -> new SystemLogItem(new Item.Properties()));

}