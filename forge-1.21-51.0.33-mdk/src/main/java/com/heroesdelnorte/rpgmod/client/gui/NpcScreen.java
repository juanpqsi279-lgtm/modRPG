package com.heroesdelnorte.rpgmod.client.gui;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NpcScreen extends Screen {

    private int currentMenu = 0; // 0 = Principal, 1 = Tradeo
    private final NpcBase npc;

    public NpcScreen(NpcBase npc) {
        super(Component.literal("Aldeano Sospechoso"));
        this.npc = npc;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int startY = this.height / 4 + 40;
        int centerX = this.width / 2;
        int btnWidth = 200;
        int btnHeight = 20;

        if (currentMenu == 0) {
            this.addRenderableWidget(Button.builder(Component.literal("¿Dónde se encuentra EvilMario?"), b -> {
                executeAction(1);
                this.onClose();
            }).bounds(centerX - 100, startY, btnWidth, btnHeight).build());

            this.addRenderableWidget(Button.builder(Component.literal("Saludar"), b -> {
                executeAction(2);
                this.onClose();
            }).bounds(centerX - 100, startY + 25, btnWidth, btnHeight).build());

            this.addRenderableWidget(Button.builder(Component.literal("Comerciar / Misiones"), b -> {
                if (npc.getQuestStage() == 5) {
                    executeAction(3); // Ya terminó
                    this.onClose();
                } else if (npc.isQuestActive()) {
                    executeAction(3); // Intentar entregar los objetos
                    this.onClose();
                } else {
                    this.currentMenu = 1; // Abrir la confirmación
                    this.init();
                }
            }).bounds(centerX - 100, startY + 50, btnWidth, btnHeight).build());

        } else if (currentMenu == 1) { // MENÚ DE CONFIRMACIÓN
            this.addRenderableWidget(Button.builder(Component.literal("¡Acepto!"), b -> {
                executeAction(4);
                this.onClose();
            }).bounds(centerX - 100, startY + 20, btnWidth, btnHeight).build());

            this.addRenderableWidget(Button.builder(Component.literal("Mejor en otro momento..."), b -> {
                executeAction(5);
                this.onClose();
            }).bounds(centerX - 100, startY + 45, btnWidth, btnHeight).build());
        }
    }

    private void executeAction(int action) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.connection.sendCommand("rpg_npc " + action + " " + npc.getId());
        }
    }

    // --- AQUÍ ESTÁ LA SOLUCIÓN AL DESENFOQUE ---

    // 1. Quitamos el filtro de desenfoque de Minecraft 1.21 en el fondo
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
    }

    // 2. Renderizamos los botones y textos en el orden correcto
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dibuja el fondo y los botones
        super.render(graphics, mouseX, mouseY, partialTick);

        // Dibuja los textos al final usando tu misión dinámica
        int centerX = this.width / 2;

        if (currentMenu == 0) {
            graphics.drawCenteredString(this.font, "§lALDEANO SOSPECHOSO", centerX, this.height / 4, 0xFFD700);
            graphics.drawCenteredString(this.font, "¿Qué necesitas, forastero?", centerX, this.height / 4 + 15, 0xFFFFFF);
        } else if (currentMenu == 1) {
            graphics.drawCenteredString(this.font, "§lMISIÓN DISPONIBLE", centerX, this.height / 4, 0xFFAA00);
            String req = getRequirementString(npc.getQuestStage());
            graphics.drawCenteredString(this.font, "Necesito que me consigas " + req + ". ¿Aceptas?", centerX, this.height / 4 + 15, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private String getRequirementString(int stage) {
        return switch (stage) {
            case 0 -> "8 Bits Corruptos";
            case 1 -> "15 Bits Corruptos";
            case 2 -> "20 Bits Corruptos";
            case 3 -> "25 Bits Corruptos";
            default -> "Nada";
        };
    }
}