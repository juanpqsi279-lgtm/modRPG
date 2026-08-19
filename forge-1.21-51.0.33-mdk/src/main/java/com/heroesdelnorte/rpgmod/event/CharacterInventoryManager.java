package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CharacterInventoryManager {

    public static void switchCharacterInventory(Player player, String oldClass, String newClass) {
        CompoundTag persistentData = player.getPersistentData();
        HolderLookup.Provider provider = player.level().registryAccess();

        // 1. Guardar Inventario Actual
        if (oldClass != null && !oldClass.isEmpty()) {
            ListTag savedItems = new ListTag();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    CompoundTag itemTag = new CompoundTag();
                    itemTag.putByte("Slot", (byte) i);
                    savedItems.add(stack.save(provider, itemTag));
                }
            }
            persistentData.put("Inventory_" + oldClass, savedItems);
            persistentData.putFloat("Health_" + oldClass, player.getHealth());
            persistentData.putInt("Food_" + oldClass, player.getFoodData().getFoodLevel());
            persistentData.putInt("XpLevel_" + oldClass, player.experienceLevel);
            persistentData.putFloat("XpProgress_" + oldClass, player.experienceProgress);
        }

        // GUARDAR CLASE ACTIVA PARA RECORDARLA AL MORIR
        persistentData.putString("ActiveRPGClass", newClass);

        // 2. Limpiar inventario
        player.getInventory().clearContent();

        // 3. Cargar Datos del Nuevo Personaje
        String key = "Inventory_" + newClass;
        if (persistentData.contains(key, Tag.TAG_LIST)) {
            ListTag savedItems = persistentData.getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < savedItems.size(); i++) {
                CompoundTag itemTag = savedItems.getCompound(i);
                int slot = itemTag.getByte("Slot") & 255;
                if (slot < player.getInventory().getContainerSize()) {
                    ItemStack stack = ItemStack.parseOptional(provider, itemTag);
                    player.getInventory().setItem(slot, stack);
                }
            }
            if (persistentData.contains("Health_" + newClass)) {
                player.setHealth(persistentData.getFloat("Health_" + newClass));
                player.getFoodData().setFoodLevel(persistentData.getInt("Food_" + newClass));
                player.experienceLevel = persistentData.getInt("XpLevel_" + newClass);
                player.experienceProgress = persistentData.getFloat("XpProgress_" + newClass);
            }
        } else {
            // Stats para primera vez
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.experienceLevel = 0;
            player.experienceProgress = 0.0f;
        }

        // 4. EL BLINDAJE: Asegurarse de que el jugador TENGA su arma
        ensureWeapon(player, newClass);

        player.sendSystemMessage(Component.literal("§a[RPG] Personaje activo: §l" + newClass));
        player.giveExperienceLevels(0);
        player.containerMenu.broadcastChanges();
        player.inventoryMenu.broadcastChanges();
    }

    // Método blindado que revisa y devuelve el arma si falta
    public static void ensureWeapon(Player player, String characterClass) {
        Item requiredWeapon = switch (characterClass) {
            case "Uriel" -> ModItems.WRENCH.get();
            case "ChatGPT" -> ModItems.WHIP.get();
            case "Josepe" -> ModItems.JOSEPE_AXE.get();
            default -> null;
        };

        if (requiredWeapon != null) {
            boolean hasWeapon = false;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(requiredWeapon)) {
                    hasWeapon = true;
                    break;
                }
            }
            if (!hasWeapon) {
                player.getInventory().add(new ItemStack(requiredWeapon));
            }
        }
    }
}