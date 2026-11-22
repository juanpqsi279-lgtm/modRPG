package com.heroesdelnorte.rpgmod.client.gui;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.SelectCharacterPayload;
import com.heroesdelnorte.rpgmod.network.UpgradeSkillPayload;
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

    public RpgMenuScreen(Player player, boolean isFirstTimeSelection) {
        super(Component.literal("Menú RPG"));
        this.player = player;
        this.isFirstTimeSelection = isFirstTimeSelection;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Botones de Selección de Clase (Columna izquierda, sin tapar el modelo central)
        this.addRenderableWidget(Button.builder(Component.literal("Uriel (Llave Inglesa)"), b -> selectCharacter("Uriel"))
                .bounds(centerX - 185, centerY - 40, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("ChatGPT (Látigo)"), b -> selectCharacter("ChatGPT"))
                .bounds(centerX - 185, centerY - 15, 140, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Josepe (Hacha)"), b -> selectCharacter("Josepe"))
                .bounds(centerX - 185, centerY + 10, 140, 20).build());

        // Botones de Habilidad (Columna derecha, dejando el centro libre)
        if (!isFirstTimeSelection) {
            this.addRenderableWidget(Button.builder(Component.literal("+ Daño (Costo: 1 SP)"), b -> upgradeSkill("daño"))
                    .bounds(centerX + 45, centerY - 40, 140, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+ Salud (Costo: 1 SP)"), b -> upgradeSkill("salud"))
                    .bounds(centerX + 45, centerY - 15, 140, 20).build());
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !isFirstTimeSelection;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
    }

    private void selectCharacter(String characterName) {
        String previousClass = RpgStats.getActiveClass(player);

        if (characterName.equals(previousClass)) {
            this.onClose();
            return;
        }

        RpgStats.setActiveClass(player, characterName);
        ModMessages.sendToServer(new SelectCharacterPayload(characterName));
        this.onClose();
    }

    private void upgradeSkill(String skill) {
        ModMessages.sendToServer(new UpgradeSkillPayload(skill));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Textos de Cabecera
        if (isFirstTimeSelection) {
            graphics.drawCenteredString(this.font, "§l¡BIENVENIDO! SELECCIONA TU PERSONAJE PARA EMPEZAR", centerX, 25, 0xFFAA00);
            graphics.drawCenteredString(this.font, "§7Debes escoger una clase para iniciar tu aventura", centerX, 45, 0xAAAAAA);
        } else {
            int bonusDamageLv = RpgStats.getBonusDamage(player.getUUID());
            int bonusHealthLv = RpgStats.getBonusHealth(player.getUUID());
            graphics.drawCenteredString(this.font, "§lMENÚ DE HÉROE", centerX, 20, 0xFFFFFF);
            graphics.drawString(this.font, "Nivel (XP): " + player.experienceLevel, centerX - 185, centerY - 70, 0x55FF55);
            graphics.drawString(this.font, "Puntos Disp.: " + player.experienceLevel, centerX + 45, centerY - 70, 0xFFFF55);
            graphics.drawString(this.font, "§c+Daño Nv. " + bonusDamageLv + " (+" + (bonusDamageLv * 5) + ")", centerX + 45, centerY + 12, 0xFFAAAA);
            graphics.drawString(this.font, "§a+Salud Nv. " + bonusHealthLv + " (+" + (bonusHealthLv * 2) + " ❤)", centerX + 45, centerY + 24, 0xAAFFAA);
        }

        // --- LÓGICA DE RENDERIZADO 3D EN VIVO ---
        String hoveredCharacter = getCharacterAtMouse(centerX, centerY, mouseX, mouseY);
        if (hoveredCharacter.isEmpty()) {
            hoveredCharacter = RpgStats.getActiveClass(player);
            if (hoveredCharacter.isEmpty()) {
                hoveredCharacter = "Uriel";
            }
        }

        drawCharacterModel(graphics, centerX, centerY + 40, mouseX, mouseY, hoveredCharacter);
    }

    private String getCharacterAtMouse(int centerX, int centerY, int mouseX, int mouseY) {
        if (mouseX >= centerX - 185 && mouseX <= centerX - 45) {
            if (mouseY >= centerY - 40 && mouseY <= centerY - 20) return "Uriel";
            if (mouseY >= centerY - 15 && mouseY <= centerY + 5) return "ChatGPT";
            if (mouseY >= centerY + 10 && mouseY <= centerY + 30) return "Josepe";
        }
        return "";
    }

    private void drawCharacterModel(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, String characterClass) {
        CompoundTag persistentData = player.getPersistentData();
        String originalClass = persistentData.getString(RpgStats.TAG_CLASS);

        try {
            persistentData.putString(RpgStats.TAG_CLASS, characterClass);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    x - 30, y - 75,
                    x + 30, y,
                    45,
                    0.0625F,
                    (float) mouseX,
                    (float) mouseY,
                    player
            );
        } finally {
            persistentData.putString(RpgStats.TAG_CLASS, originalClass);
        }
    }
}