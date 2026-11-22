package com.heroesdelnorte.rpgmod.registry;

import com.heroesdelnorte.rpgmod.item.DropTableSwordItem;
import com.heroesdelnorte.rpgmod.item.GlitchSwordItem;
import com.heroesdelnorte.rpgmod.item.RpgArmorItem;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.item.StructureMapItem;
import com.heroesdelnorte.rpgmod.item.SystemLogItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
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
            () -> new StructureMapItem(new Item.Properties().stacksTo(1)));

    // Armadura Cortafuegos (Nivel intermedio-alto)
    public static final RegistryObject<Item> FIREWALL_HELMET = ITEMS.register("firewall_helmet",
            () -> new RpgArmorItem(ModArmorMaterials.FIREWALL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(42)), "firewall"));

    public static final RegistryObject<Item> FIREWALL_CHESTPLATE = ITEMS.register("firewall_chestplate",
            () -> new RpgArmorItem(ModArmorMaterials.FIREWALL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(42)), "firewall"));

    public static final RegistryObject<Item> FIREWALL_LEGGINGS = ITEMS.register("firewall_leggings",
            () -> new RpgArmorItem(ModArmorMaterials.FIREWALL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(42)), "firewall"));

    public static final RegistryObject<Item> FIREWALL_BOOTS = ITEMS.register("firewall_boots",
            () -> new RpgArmorItem(ModArmorMaterials.FIREWALL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(42)), "firewall"));

    // Armadura del Núcleo Cuántico (Definitiva anti-EvilMario)
    public static final RegistryObject<Item> QUANTUM_HELMET = ITEMS.register("quantum_helmet",
            () -> new RpgArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(65)).fireResistant(), "quantum"));

    public static final RegistryObject<Item> QUANTUM_CHESTPLATE = ITEMS.register("quantum_chestplate",
            () -> new RpgArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(65)).fireResistant(), "quantum"));

    public static final RegistryObject<Item> QUANTUM_LEGGINGS = ITEMS.register("quantum_leggings",
            () -> new RpgArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(65)).fireResistant(), "quantum"));

    public static final RegistryObject<Item> QUANTUM_BOOTS = ITEMS.register("quantum_boots",
            () -> new RpgArmorItem(ModArmorMaterials.QUANTUM, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(65)).fireResistant(), "quantum"));

    // Exotraje ROOT ADMIN-SQL MK.I (Reliquia exclusiva de EvilMario - estilo Iron Man)
    public static final RegistryObject<Item> ADMIN_HELMET = ITEMS.register("admin_helmet",
            () -> new RpgArmorItem(ModArmorMaterials.ADMIN_SQL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(120)).fireResistant(), "admin_sql"));

    public static final RegistryObject<Item> ADMIN_CHESTPLATE = ITEMS.register("admin_chestplate",
            () -> new RpgArmorItem(ModArmorMaterials.ADMIN_SQL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(120)).fireResistant(), "admin_sql"));

    public static final RegistryObject<Item> ADMIN_LEGGINGS = ITEMS.register("admin_leggings",
            () -> new RpgArmorItem(ModArmorMaterials.ADMIN_SQL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(120)).fireResistant(), "admin_sql"));

    public static final RegistryObject<Item> ADMIN_BOOTS = ITEMS.register("admin_boots",
            () -> new RpgArmorItem(ModArmorMaterials.ADMIN_SQL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(120)).fireResistant(), "admin_sql"));

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
            () -> new GlitchSwordItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .fireResistant()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4F))));

    public static final RegistryObject<Item> DROP_TABLE_SWORD = ITEMS.register("drop_table_sword",
            () -> new DropTableSwordItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .durability(2500)
                            .fireResistant()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -2.0F))));

    public static final RegistryObject<Item> CORRUPTED_BIT = ITEMS.register("corrupted_bit",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SYSTEM_LOG = ITEMS.register("system_log",
            () -> new SystemLogItem(new Item.Properties()));

}