package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.client.gui.RpgMenuScreen;
import com.heroesdelnorte.rpgmod.item.RpgArmorItem;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.network.FireRepulsorPayload;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "rpgmod", value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping OPEN_RPG_MENU = new KeyMapping(
            "key.rpgmod.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.gameplay"
    );

    public static final KeyMapping FIRE_REPULSOR = new KeyMapping(
            "key.rpgmod.repulsor_beam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.gameplay"
    );

    private static boolean pendingInitialSelection = false;

    public static void requestInitialSelection() {
        pendingInitialSelection = true;
    }

    public static void clearInitialSelection() {
        pendingInitialSelection = false;
    }

    // Al entrar al mundo sin clase en el servidor, abre la pantalla obligatoria
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                if (pendingInitialSelection && mc.screen == null) {
                    pendingInitialSelection = false;
                    mc.setScreen(new RpgMenuScreen(mc.player, true));
                }
            } else {
                pendingInitialSelection = false;
            }
        }
    }

    // Abrir menú (R) o disparar Rayo Repulsor SQL (V) durante el juego
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (OPEN_RPG_MENU.consumeClick()) {
            mc.setScreen(new RpgMenuScreen(mc.player, false));
        }
        if (FIRE_REPULSOR.consumeClick() && RpgArmorItem.hasFullAdminSqlSet(mc.player)) {
            ModMessages.sendToServer(new FireRepulsorPayload());
        }
    }

    // Disparo alternativo del Rayo Repulsor: Shift + Clic Derecho con mano vacía
    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null
                && event.getHand() == InteractionHand.MAIN_HAND
                && event.getEntity() == mc.player
                && event.getEntity().isShiftKeyDown()
                && RpgArmorItem.hasFullAdminSqlSet(event.getEntity())) {
            ModMessages.sendToServer(new FireRepulsorPayload());
        }
    }

    @Mod.EventBusSubscriber(modid = "rpgmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(OPEN_RPG_MENU);
            event.register(FIRE_REPULSOR);
        }
    }

    // Bloquea la tecla 'Q' (soltar ítem) si tienes un arma RPG en la mano
    @SubscribeEvent
    public static void onDropKeyPress(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.screen == null && mc.options.keyDrop.isDown()) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (heldItem.getItem() instanceof RpgWeapon) {
                while (mc.options.keyDrop.consumeClick()) {}
            }
        }
    }
}
