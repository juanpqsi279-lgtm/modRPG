package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CharacterInventoryManager {

    /**
     * Guarda el inventario del personaje saliente y carga el del nuevo personaje.
     */
    public static void switchCharacterInventory(Player player, String oldClass, String newClass) {
        CompoundTag persistentData = player.getPersistentData();
        HolderLookup.Provider provider = player.level().registryAccess();

        // 1. Guardar el inventario actual si venía de un personaje válido
        if (oldClass != null && !oldClass.isEmpty()) {
            ListTag savedItems = new ListTag();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putByte("Slot", (byte) i);
                    // En 1.21 se guarda pasando el provider de registros
                    savedItems.add(stack.save(provider, itemTag));
                }
            }
            persistentData.put("Inventory_" + oldClass, savedItems);
        }

        // 2. Limpiar todo el inventario actual
        player.getInventory().clearContent();

        // 3. Cargar o inicializar el inventario del nuevo personaje
        String key = "Inventory_" + newClass;
        if (persistentData.contains(key, Tag.TAG_LIST)) {
            ListTag savedItems = persistentData.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < savedItems.size(); i++) {
                CompoundTag itemTag = savedItems.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < player.getInventory().getContainerSize()) {
                    // En 1.21 se parsea con parseOptional
                    ItemStack stack = ItemStack.parseOptional(provider, itemTag);
                    player.getInventory().setItem(slot, stack);
                }
            }
        } else {
            // Primera vez con este personaje: se le da su arma exclusiva
            giveStartingWeapon(player, newClass);
        }

        // 4. Asegurarse de que siempre tenga su arma correspondiente en su inventario
        ensureHasClassWeapon(player, newClass);

        // 5. Sincronizar el inventario con el cliente inmediatamente
        player.containerMenu.broadcastChanges();
    }

    private static void giveStartingWeapon(Player player, String characterClass) {
        switch (characterClass) {
            case "Uriel" -> player.getInventory().add(new ItemStack(ModItems.WRENCH.get()));
            case "ChatGPT" -> player.getInventory().add(new ItemStack(ModItems.WHIP.get()));
            case "Josepe" -> player.getInventory().add(new ItemStack(ModItems.JOSEPE_AXE.get()));
        }
    }

    private static void ensureHasClassWeapon(Player player, String characterClass) {
        ItemStack targetWeapon = switch (characterClass) {
            case "Uriel" -> new ItemStack(ModItems.WRENCH.get());
            case "ChatGPT" -> new ItemStack(ModItems.WHIP.get());
            case "Josepe" -> new ItemStack(ModItems.JOSEPE_AXE.get());
            default -> ItemStack.EMPTY;
        };

        if (!targetWeapon.isEmpty() && !player.getInventory().contains(targetWeapon)) {
            player.getInventory().add(targetWeapon);
        }
    }
}