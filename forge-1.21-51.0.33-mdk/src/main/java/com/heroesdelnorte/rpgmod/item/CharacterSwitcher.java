package com.heroesdelnorte.rpgmod.item;

import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CharacterSwitcher extends Item {
    public CharacterSwitcher(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            // 1. Revisa qué personaje eres actualmente
            String current = getCurrentCharacter(player);

            // 2. Limpia el inventario de CUALQUIER arma RPG para evitar duplicados
            clearRpgWeapons(player);

            // 3. Asigna la nueva clase y arma
            if (current.equals("None") || current.equals("Josepe")) {
                player.getInventory().add(new ItemStack(ModItems.WRENCH.get()));
                player.sendSystemMessage(Component.literal("§6¡Has cambiado a URIEL! Arma: Llave Inglesa."));
            } else if (current.equals("Uriel")) {
                player.getInventory().add(new ItemStack(ModItems.WHIP.get()));
                player.sendSystemMessage(Component.literal("§b¡Has cambiado a CHATGPT! Arma: Látigo."));
            } else if (current.equals("ChatGPT")) {
                player.getInventory().add(new ItemStack(ModItems.JOSEPE_AXE.get()));
                player.sendSystemMessage(Component.literal("§c¡Has cambiado a JOSEPE! Arma: Hacha."));
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private void clearRpgWeapons(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.WRENCH.get()) || stack.is(ModItems.WHIP.get()) || stack.is(ModItems.JOSEPE_AXE.get())) {
                player.getInventory().removeItemNoUpdate(i);
            }
        }
    }

    private String getCurrentCharacter(Player player) {
        if (player.getInventory().contains(new ItemStack(ModItems.WRENCH.get()))) return "Uriel";
        if (player.getInventory().contains(new ItemStack(ModItems.WHIP.get()))) return "ChatGPT";
        if (player.getInventory().contains(new ItemStack(ModItems.JOSEPE_AXE.get()))) return "Josepe";
        return "None";
    }
}