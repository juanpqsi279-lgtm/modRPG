package com.heroesdelnorte.rpgmod.client.gui;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.SelectCharacterPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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

        // Botones de Selección de Clase (Alineados a la izquierda)
        this.addRenderableWidget(Button.builder(Component.literal("Uriel (Llave Inglesa)"), b -> selectCharacter("Uriel"))
                .bounds(centerX - 150, centerY - 40, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("ChatGPT (Látigo)"), b -> selectCharacter("ChatGPT"))
                .bounds(centerX - 150, centerY - 15, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Josepe (Hacha)"), b -> selectCharacter("Josepe"))
                .bounds(centerX - 150, centerY + 10, 140, 20).build());

        // Botones de Habilidad (Alineados a la derecha, dejando el centro libre)
        if (!isFirstTimeSelection) {
            this.addRenderableWidget(Button.builder(Component.literal("+ Daño (Costo: 1 SP)"), b -> upgradeSkill("daño"))
                    .bounds(centerX + 10, centerY - 40, 140, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+ Salud (Costo: 1 SP)"), b -> upgradeSkill("salud"))
                    .bounds(centerX + 10, centerY - 15, 140, 20).build());
        }
    }

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

        persistentData.putString("RpgModClass", characterName);
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

        // Textos de Cabecera
        if (isFirstTimeSelection) {
            graphics.drawCenteredString(this.font, "§l¡BIENVENIDO! SELECCIONA TU PERSONAJE PARA EMPEZAR", centerX, 25, 0xFFAA00);
            graphics.drawCenteredString(this.font, "§7Debes escoger una clase para iniciar tu aventura", centerX, 45, 0xAAAAAA);
        } else {
            graphics.drawCenteredString(this.font, "§lMENÚ DE HÉROE", centerX, 20, 0xFFFFFF);
            graphics.drawString(this.font, "Nivel (XP): " + player.experienceLevel, centerX - 140, centerY - 70, 0x55FF55);
            graphics.drawString(this.font, "Puntos Disp.: " + this.skillPoints, centerX + 10, centerY - 70, 0xFFFF55);
        }

        // --- LÓGICA DE RENDERIZADO 3D EN VIVO ---

        // 1. Detectar qué clase está siendo apuntada por el ratón
        String hoveredCharacter = getCharacterAtMouse(centerX, centerY, mouseX, mouseY);

        // 2. Si no apunta a nada, mostrar su clase actual (o Uriel por defecto)
        if (hoveredCharacter.isEmpty()) {
            hoveredCharacter = player.getPersistentData().getString("RpgModClass");
            if (hoveredCharacter.isEmpty()) hoveredCharacter = "Uriel";
        }

        // 3. Dibujar al personaje en el centro de la pantalla
        drawCharacterModel(graphics, centerX - 5, centerY + 40, mouseX, mouseY, hoveredCharacter);
    }

    // Método auxiliar para saber en qué botón está el ratón basado en coordenadas
    private String getCharacterAtMouse(int centerX, int centerY, int mouseX, int mouseY) {
        if (mouseX >= centerX - 150 && mouseX <= centerX - 10) {
            if (mouseY >= centerY - 40 && mouseY <= centerY - 20) return "Uriel";
            if (mouseY >= centerY - 15 && mouseY <= centerY + 5) return "ChatGPT";
            if (mouseY >= centerY + 10 && mouseY <= centerY + 30) return "Josepe";
        }
        return "";
    }

    // La magia visual ocurre aquí
    private void drawCharacterModel(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, String characterClass) {
        CompoundTag persistentData = player.getPersistentData();
        String originalClass = persistentData.getString("RpgModClass");

        // Engañamos temporalmente a tu PlayerSkinHandler
        persistentData.putString("RpgModClass", characterClass);

        // Renderizado nativo de Minecraft 1.21 (Requiere caja delimitadora de 10 argumentos)
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                x - 30, y - 75, // Esquina superior izquierda de la caja (minX, minY)
                x + 30, y,      // Esquina inferior derecha de la caja (maxX, maxY)
                45,             // Escala (pScale)
                0.0625F,        // Factor de escala interno (pScaleFactor)
                (float) mouseX, // Posición del ratón en pantalla (mouseX)
                (float) mouseY, // Posición del ratón en pantalla (mouseY)
                player          // La entidad a dibujar
        );

        // Restauramos la realidad
        persistentData.putString("RpgModClass", originalClass);
    }
}