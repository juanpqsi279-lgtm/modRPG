package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CharacterInventoryManager {

    public static void switchCharacterInventory(Player player, String oldClass, String newClass) {
        if (!RpgStats.isValidClass(newClass)) {
            return;
        }

        CompoundTag persistentData = player.getPersistentData();
        HolderLookup.Provider provider = player.level().registryAccess();

        // 1. Guardar inventario y estadísticas del personaje saliente
        if (oldClass != null && !oldClass.isEmpty() && RpgStats.isValidClass(oldClass)) {
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
            persistentData.putFloat("Saturation_" + oldClass, player.getFoodData().getSaturationLevel());
            persistentData.putInt("XpLevel_" + oldClass, player.experienceLevel);
            persistentData.putFloat("XpProgress_" + oldClass, player.experienceProgress);
        }

        // 2. Actualizar clase activa y aplicar atributos ANTES de restaurar la vida
        RpgStats.setActiveClass(player, newClass);
        if (player instanceof ServerPlayer serverPlayer) {
            RpgStats.applyAllPlayerAttributes(serverPlayer);
        }

        // 3. Limpiar inventario actual
        player.getInventory().clearContent();

        // 4. Cargar datos del nuevo personaje
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
                float savedHealth = persistentData.getFloat("Health_" + newClass);
                player.setHealth(Math.max(1.0F, Math.min(savedHealth, player.getMaxHealth())));
                player.getFoodData().setFoodLevel(persistentData.getInt("Food_" + newClass));
                if (persistentData.contains("Saturation_" + newClass)) {
                    player.getFoodData().setSaturation(persistentData.getFloat("Saturation_" + newClass));
                }
                player.experienceLevel = persistentData.getInt("XpLevel_" + newClass);
                player.experienceProgress = persistentData.getFloat("XpProgress_" + newClass);
            } else {
                player.setHealth(player.getMaxHealth());
            }
        } else {
            // Stats iniciales para primera vez con esta clase
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0F);
            player.experienceLevel = 0;
            player.experienceProgress = 0.0f;
        }

        // 5. Asegurar que el jugador tenga su arma de clase
        ensureWeapon(player, newClass);

        player.sendSystemMessage(Component.literal("§a[RPG] Personaje activo: §l" + newClass));
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetExperiencePacket(
                    serverPlayer.experienceProgress,
                    serverPlayer.totalExperience,
                    serverPlayer.experienceLevel
            ));
        }
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
                ItemStack weaponStack = new ItemStack(requiredWeapon);
                if (!player.getInventory().add(weaponStack)) {
                    ItemStack displaced = player.getInventory().getItem(0).copy();
                    player.getInventory().setItem(0, weaponStack);
                    if (!displaced.isEmpty()) {
                        player.drop(displaced, false);
                    }
                }
            }
        }
    }
}