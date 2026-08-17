package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.client.gui.RpgMenuScreen;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
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

    private static boolean checkedInitialLogin = false;

    // Al entrar al mundo, si no tiene clase guardada, le abre la pantalla a la fuerza
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                if (!checkedInitialLogin) {
                    Player player = mc.player;
                    String currentClass = player.getPersistentData().getString("RpgModClass");

                    if (currentClass.isEmpty()) {
                        // Abre pantalla obligatoria (no cerrable con ESC)
                        mc.setScreen(new RpgMenuScreen(player, true));
                    }
                    checkedInitialLogin = true;
                }
            } else {
                checkedInitialLogin = false; // Se resetea al salir del mundo
            }
        }
    }

    // Abrir menú con la tecla R durante el juego
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (OPEN_RPG_MENU.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreen(new RpgMenuScreen(mc.player, false));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "rpgmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(OPEN_RPG_MENU);
        }
    }

    // Bloquea la tecla 'Q' (soltar ítem) si tienes un arma RPG en la mano
    @SubscribeEvent
    public static void onDropKeyPress(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.options.keyDrop.isDown()) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (heldItem.is(ModItems.WRENCH.get()) ||
                    heldItem.is(ModItems.WHIP.get()) ||
                    heldItem.is(ModItems.JOSEPE_AXE.get())) {

                // Consumir el click para que el cliente no haga la animación de tirar
                while (mc.options.keyDrop.consumeClick()) {}
            }
        }
    }
}