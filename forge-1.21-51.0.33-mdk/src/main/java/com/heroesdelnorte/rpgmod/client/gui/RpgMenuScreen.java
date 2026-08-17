package com.heroesdelnorte.rpgmod.client.gui;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.SelectCharacterPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class RpgMenuScreen extends Screen {
    private final Player player;
    private final boolean isFirstTimeSelection;
    private int skillPoints;

    public RpgMenuScreen(Player player, boolean isFirstTimeSelection) {
        super(Component.literal("Menú RPG"));
        this.player = player;
        this.isFirstTimeSelection = isFirstTimeSelection;
        this.skillPoints = player.experienceLevel;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Botones de Selección de Clase
        this.addRenderableWidget(Button.builder(Component.literal("Uriel (Llave Inglesa)"), b -> selectCharacter("Uriel"))
                .bounds(centerX - 150, centerY - 40, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("ChatGPT (Látigo)"), b -> selectCharacter("ChatGPT"))
                .bounds(centerX - 150, centerY - 15, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Josepe (Hacha)"), b -> selectCharacter("Josepe"))
                .bounds(centerX - 150, centerY + 10, 140, 20).build());

        // Botones de Habilidad (Solo visibles si no es la pantalla de bienvenida obligatoria)
        if (!isFirstTimeSelection) {
            this.addRenderableWidget(Button.builder(Component.literal("+ Daño (Costo: 1 SP)"), b -> upgradeSkill("daño"))
                    .bounds(centerX + 10, centerY - 40, 140, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+ Salud (Costo: 1 SP)"), b -> upgradeSkill("salud"))
                    .bounds(centerX + 10, centerY - 15, 140, 20).build());
        }
    }

    // Bloquea cerrar con la tecla ESC si aún no ha seleccionado clase
    @Override
    public boolean shouldCloseOnEsc() {
        return !isFirstTimeSelection;
    }

    private void selectCharacter(String characterName) {
        CompoundTag persistentData = player.getPersistentData();
        String previousClass = persistentData.getString("RpgModClass");

        if (characterName.equals(previousClass)) {
            this.onClose();
            return;
        }

        // 1. Actualizar dato local para cambio instantáneo de skin en el cliente
        persistentData.putString("RpgModClass", characterName);

        // 2. Enviar paquete al Servidor para que maneje el inventario real y los ítems
        ModMessages.sendToServer(new SelectCharacterPayload(characterName));

        this.onClose();
    }

    private void upgradeSkill(String skill) {
        if (player.experienceLevel > 0) {
            player.giveExperienceLevels(-1);
            this.skillPoints = player.experienceLevel;

            if (skill.equals("daño")) {
                RpgStats.addBonusDamage(player.getUUID());
                player.sendSystemMessage(Component.literal("§c⚔ ¡Daño aumentado! (+5 extra)"));
            } else if (skill.equals("salud")) {
                RpgStats.addBonusHealth(player.getUUID());
                player.sendSystemMessage(Component.literal("§a❤ ¡Salud aumentada! (+2 corazones)"));
            }
        } else {
            player.sendSystemMessage(Component.literal("§c¡No tienes suficiente XP/Puntos!"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (isFirstTimeSelection) {
            graphics.drawCenteredString(this.font, "§l¡BIENVENIDO! SELECCIONA TU PERSONAJE PARA EMPEZAR", centerX, 25, 0xFFAA00);
            graphics.drawCenteredString(this.font, "§7Debes escoger una clase para iniciar tu aventura", centerX, 45, 0xAAAAAA);
        } else {
            graphics.drawCenteredString(this.font, "§lMENÚ DE HÉROE", centerX, 20, 0xFFFFFF);
            graphics.drawString(this.font, "Nivel (XP): " + player.experienceLevel, centerX - 140, centerY - 70, 0x55FF55);
            graphics.drawString(this.font, "Puntos Disp.: " + this.skillPoints, centerX + 20, centerY - 70, 0xFFFF55);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}