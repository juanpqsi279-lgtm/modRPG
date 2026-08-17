package com.heroesdelnorte.rpgmod.registry;

import com.heroesdelnorte.rpgmod.item.CharacterSwitcher;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "rpgmod");

    // 1. Llave inglesa de Uriel
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench",
            () -> new RpgWeapon(new Item.Properties()));

    // 2. Látigo de ChatGPT
    public static final RegistryObject<Item> WHIP = ITEMS.register("whip",
            () -> new RpgWeapon(new Item.Properties()));

    // 3. Hacha de Josepe
    public static final RegistryObject<Item> JOSEPE_AXE = ITEMS.register("josepe_axe",
            () -> new RpgWeapon(new Item.Properties()));
    // Ítem para cambiar de personaje (Puedes usar la textura de una estrella o un libro)
    public static final RegistryObject<Item> SELECTOR = ITEMS.register("class_selector",
            () -> new CharacterSwitcher(new Item.Properties().stacksTo(1)));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}