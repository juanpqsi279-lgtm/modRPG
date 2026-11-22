package com.heroesdelnorte.rpgmod.client.gui;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.NpcActionPayload;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class NpcScreen extends Screen {

    private static final int TAB_TALK = 0;
    private static final int TAB_QUEST = 1;
    private static final int TAB_SHOP = 2;
    private static final int TAB_JOURNAL = 3;

    private static final int TAB_PRISONER_FORGE = 0;
    private static final int TAB_PRISONER_TALK = 1;

    private int activeTab = 0;
    private String dialogueText;
    private final NpcBase npc;

    public NpcScreen(NpcBase npc) {
        super(Component.literal(npc.getNpcName()));
        this.npc = npc;
        this.dialogueText = npc.isPrisoner()
                ? "§b¡Ayuda, héroe! Llevamos meses encerrados viendo a EvilMario operar el Núcleo. Tráeme §d4 Fragmentos §by §c1 Bloque de Redstone §by compilaré tu §6Código de Invocación§b."
                : "§fBienvenido, sobreviviente. Elige cualquier pestaña arriba para investigar el Incidente 404, avanzar en la historia o comerciar.";
    }

    public int getNpcId() {
        return npc.getId();
    }

    public void setDialogueResponse(String text) {
        if (text != null && !text.isEmpty()) {
            this.dialogueText = text;
        }
    }

    public void refreshState() {
        this.init();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int panelTop = this.height / 2 - 108;
        int panelLeft = centerX - 190;
        int panelRight = centerX + 190;
        int btnHeight = 18;

        if (npc.isPrisoner()) {
            initPrisonerWidgets(centerX, panelTop, panelLeft, panelRight, btnHeight);
        } else {
            initVillagerWidgets(centerX, panelTop, panelLeft, panelRight, btnHeight);
        }
    }

    private void initPrisonerWidgets(int centerX, int panelTop, int panelLeft, int panelRight, int btnHeight) {
        int tabY = panelTop + 38;
        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_PRISONER_FORGE ? "§b§l[🔑 Compilar Key]" : "🔑 Compilar Key"), b -> {
            this.activeTab = TAB_PRISONER_FORGE;
            this.init();
        }).bounds(panelLeft + 10, tabY, 108, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_PRISONER_TALK ? "§b§l[💬 Interrogar]" : "💬 Interrogar"), b -> {
            this.activeTab = TAB_PRISONER_TALK;
            this.init();
        }).bounds(panelLeft + 122, tabY, 104, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("💚 Curar"), b -> sendAction(2, 1))
                .bounds(panelLeft + 230, tabY, 74, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("✕ Cerrar"), b -> this.onClose())
                .bounds(panelRight - 72, tabY, 62, btnHeight).build());

        int contentY = panelTop + 64;
        if (activeTab == TAB_PRISONER_FORGE) {
            this.addRenderableWidget(Button.builder(Component.literal("§6§l🔑 Compilar y Soltar Key (4 Frag + 1 Redstone)"), b -> sendAction(11, 0))
                    .bounds(centerX - 165, contentY + 40, 330, 22).build());

            this.addRenderableWidget(Button.builder(Component.literal("⚡ Canje Rápido: 8 Bits Corruptos → 1 Bloque de Redstone"), b -> sendAction(12, 0))
                    .bounds(centerX - 165, contentY + 66, 330, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.literal("¿Cómo los encerró EvilMario aquí?"), b -> sendAction(10, 0))
                    .bounds(centerX - 165, contentY + 2, 330, 19).build());
            this.addRenderableWidget(Button.builder(Component.literal("¿Cómo aprendieron a crear la Key?"), b -> sendAction(10, 1))
                    .bounds(centerX - 165, contentY + 23, 330, 19).build());
            this.addRenderableWidget(Button.builder(Component.literal("¿Qué reliquias oculta en la cima blanca?"), b -> sendAction(10, 2))
                    .bounds(centerX - 165, contentY + 44, 330, 19).build());
        }
    }

    private void initVillagerWidgets(int centerX, int panelTop, int panelLeft, int panelRight, int btnHeight) {
        int tabY = panelTop + 38;
        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_TALK ? "§e§l[Hablar]" : "Hablar"), b -> {
            this.activeTab = TAB_TALK;
            this.init();
        }).bounds(panelLeft + 8, tabY, 56, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_QUEST ? "§e§l[Historia]" : "Historia"), b -> {
            this.activeTab = TAB_QUEST;
            this.init();
        }).bounds(panelLeft + 66, tabY, 64, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_SHOP ? "§e§l[Tienda]" : "Tienda"), b -> {
            this.activeTab = TAB_SHOP;
            this.init();
        }).bounds(panelLeft + 132, tabY, 56, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal(activeTab == TAB_JOURNAL ? "§e§l[Diario]" : "Diario"), b -> {
            this.activeTab = TAB_JOURNAL;
            this.init();
        }).bounds(panelLeft + 190, tabY, 54, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("💚 Curar"), b -> sendAction(2, 1))
                .bounds(panelLeft + 248, tabY, 68, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.literal("✕"), b -> this.onClose())
                .bounds(panelRight - 58, tabY, 50, btnHeight).build());

        Player player = this.minecraft != null ? this.minecraft.player : null;
        int playerBits = countItem(player, ModItems.CORRUPTED_BIT.get());
        int contentY = panelTop + 64;

        if (activeTab == TAB_TALK) {
            int halfW = 174;
            int col1 = centerX - 178;
            int col2 = centerX + 4;
            this.addRenderableWidget(Button.builder(Component.literal("El Incidente Línea 404"), b -> sendAction(1, 0))
                    .bounds(col1, contentY + 2, halfW, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Bits y Fragmentos"), b -> sendAction(1, 1))
                    .bounds(col2, contentY + 2, halfW, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Ubicación de la Torre"), b -> sendAction(1, 2))
                    .bounds(col1, contentY + 26, halfW, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Prisioneros, Key y ROOT"), b -> sendAction(1, 3))
                    .bounds(col2, contentY + 26, halfW, 20).build());
        } else if (activeTab == TAB_QUEST) {
            int stage = RpgStats.getQuestStage(player);
            boolean active = RpgStats.isQuestActive(player);
            int actionY = contentY + 62;
            if (stage >= 4) {
                this.addRenderableWidget(Button.builder(Component.literal("🗺 Reimprimir Mapa de la Torre de Base de Datos"), b -> sendAction(7, 0))
                        .bounds(centerX - 155, actionY, 310, 22).build());
            } else if (active) {
                this.addRenderableWidget(Button.builder(Component.literal("✓ Entregar Bits y Completar Capítulo " + (stage + 1) + "/4"), b -> sendAction(3, 0))
                        .bounds(centerX - 155, actionY, 310, 22).build());
            } else {
                this.addRenderableWidget(Button.builder(Component.literal("! Aceptar Capítulo " + (stage + 1) + "/4"), b -> sendAction(4, 0))
                        .bounds(centerX - 155, actionY, 310, 22).build());
            }
        } else if (activeTab == TAB_SHOP) {
            int colL = centerX - 180;
            int colR = centerX + 4;
            int w = 176;
            int rowH = 20;
            addShopButton(colL, contentY + 2, w, rowH, 0, 3, "Manzanas + Filetes", playerBits);
            addShopButton(colR, contentY + 2, w, rowH, 1, 4, "8 Botellas de XP", playerBits);
            addShopButton(colL, contentY + 24, w, rowH, 2, 5, "Registro del Sistema", playerBits);
            addShopButton(colR, contentY + 24, w, rowH, 3, 6, "Tótem Inmortal", playerBits);
            addShopButton(colL, contentY + 46, w, rowH, 4, 8, "Bloque Redstone", playerBits);
            addShopButton(colR, contentY + 46, w, rowH, 5, 8, "Manzana Encantada", playerBits);
            addShopButton(colL, contentY + 68, w, rowH, 6, 10, "Pieza Cortafuegos", playerBits);
            addShopButton(colR, contentY + 68, w, rowH, 7, 15, "Pieza Cuántica", playerBits);
        }
    }

    private void addShopButton(int x, int y, int w, int h, int offerId, int cost, String label, int playerBits) {
        String c = playerBits >= cost ? "§a" : "§7";
        this.addRenderableWidget(Button.builder(Component.literal(c + cost + "B → §f" + label), b -> sendAction(6, offerId))
                .bounds(x, y, w, h).build());
    }

    private void sendAction(int actionId, int param) {
        ModMessages.sendToServer(new NpcActionPayload(npc.getId(), actionId, param));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int panelTop = this.height / 2 - 108;
        int panelLeft = centerX - 190;
        int panelRight = centerX + 190;
        int panelBottom = this.height / 2 + 108;

        int borderColor = npc.isPrisoner() ? 0xCC00CCFF : 0xCCFFAA00;
        graphics.fill(panelLeft - 1, panelTop - 1, panelRight + 1, panelBottom + 1, borderColor);
        graphics.fill(panelLeft, panelTop, panelRight, panelBottom, 0xDF10131C);

        super.render(graphics, mouseX, mouseY, partialTick);

        Player player = this.minecraft != null ? this.minecraft.player : null;
        int bits = countItem(player, ModItems.CORRUPTED_BIT.get());
        int frags = countItem(player, ModItems.BOSS_FRAGMENT.get());
        int redstone = countItem(player, Items.REDSTONE_BLOCK);
        int stage = RpgStats.getQuestStage(player);
        boolean active = RpgStats.isQuestActive(player);

        // Encabezado
        String titlePrefix = npc.isPrisoner() ? "§b§l[PRISIONERO] " : "§6§l";
        graphics.drawCenteredString(this.font, titlePrefix + npc.getNpcName().toUpperCase(), centerX, panelTop + 8, 0xFFFFFF);

        if (npc.isPrisoner()) {
            String mats = "§dFrag: " + (frags >= 4 ? "§a" : "§c") + frags + "/4  §7|  §cRedstone: "
                    + (redstone >= 1 ? "§a" : "§c") + redstone + "/1  §7|  §eBits: §f" + bits;
            graphics.drawCenteredString(this.font, mats, centerX, panelTop + 23, 0xFFFFFF);

            int contentY = panelTop + 64;
            if (activeTab == TAB_PRISONER_FORGE) {
                graphics.drawCenteredString(this.font, "§bCompilador Clandestino de Código de Invocación", centerX, contentY + 4, 0xFFFFFF);
                graphics.drawCenteredString(this.font, "§7Entréganos 4 Fragmentos + 1 Bloque de Redstone y te soltaremos la Key.", centerX, contentY + 18, 0xCCCCCC);
            }
            drawDialogueBox(graphics, panelLeft + 12, panelBottom - 54, 356, 46, dialogueText);
            return;
        }

        // Subtítulo para Aldeanos Sobrevivientes
        String stageText = RpgStats.isBossDefeated(player)
                ? "§a✓ EvilMario Derrotado"
                : (stage >= 4 ? "§bTorre Desbloqueada" : "§eCap. " + (stage + 1) + "/4" + (active ? " §a(Activa)" : " §7(Disp.)"));
        graphics.drawString(this.font, stageText, panelLeft + 10, panelTop + 23, 0xFFFFFF);

        String invSummary = "§dBits: §f" + bits + "  §7|  §bFrag: §f" + frags + "/4";
        int invW = this.font.width(invSummary);
        graphics.drawString(this.font, invSummary, panelRight - 10 - invW, panelTop + 23, 0xFFFFFF);

        int contentY = panelTop + 64;
        if (activeTab == TAB_TALK) {
            drawDialogueBox(graphics, panelLeft + 12, contentY + 52, 356, 86, dialogueText);
        } else if (activeTab == TAB_QUEST) {
            if (stage >= 4) {
                graphics.drawCenteredString(this.font, "§a§l¡Todos los capítulos completados!", centerX, contentY + 4, 0x55FF55);
                BlockPos pos = RpgStats.getPyramidPos(player);
                String loc = pos != null ? "§6Torre: §fX=" + pos.getX() + ", Y=" + pos.getY() + ", Z=" + pos.getZ() : "§eUsa tu mapa o pulsa reimprimir.";
                graphics.drawCenteredString(this.font, loc, centerX, contentY + 20, 0xFFFFFF);
                graphics.drawCenteredString(this.font, "§bEntra a la Torre, habla con los Prisioneros y compila la Key.", centerX, contentY + 36, 0xCCCCCC);
            } else {
                int req = getRequirementAmount(stage);
                String colorCount = bits >= req ? "§a" : "§c";
                graphics.drawCenteredString(this.font, "§6Capítulo " + (stage + 1) + "/4 — §f" + getChapterTitle(stage), centerX, contentY + 4, 0xFFD700);
                graphics.drawCenteredString(this.font, "§eRequisito: §f" + req + " Bits Corruptos (" + colorCount + bits + "/" + req + "§f)", centerX, contentY + 22, 0xFFFFFF);
                graphics.drawCenteredString(this.font, "§bRecompensa: §f" + getRewardDescription(stage), centerX, contentY + 38, 0xFFFFFF);
            }
            drawDialogueBox(graphics, panelLeft + 12, panelBottom - 54, 356, 46, dialogueText);
        } else if (activeTab == TAB_SHOP) {
            drawDialogueBox(graphics, panelLeft + 12, panelBottom - 44, 356, 36, dialogueText);
        } else if (activeTab == TAB_JOURNAL) {
            renderStoryJournal(graphics, panelLeft + 16, contentY + 4, player, stage);
        }
    }

    private void renderStoryJournal(GuiGraphics graphics, int x, int y, Player player, int stage) {
        boolean hasMap = RpgStats.hasPyramidCoords(player);
        boolean forged = RpgStats.isKeyForged(player);
        boolean killed = RpgStats.isBossDefeated(player);

        graphics.drawString(this.font, "§6§lCRÓNICA DEL SERVIDOR (LÍNEA 404 → ROOT)", x, y, 0xFFD700);
        drawChecklistLine(graphics, x, y + 18, stage >= 1, "1. Contener el fallo 404 y conseguir el 1er Fragmento");
        drawChecklistLine(graphics, x, y + 34, stage >= 2, "2. Forjar Armadura Cortafuegos y 2do Fragmento");
        drawChecklistLine(graphics, x, y + 50, stage >= 3, "3. Obtener la Espada Glitch y 3er Fragmento");
        drawChecklistLine(graphics, x, y + 66, stage >= 4 && hasMap, "4. Descifrar coordenadas de la Torre de Base de Datos");
        drawChecklistLine(graphics, x, y + 82, forged, "5. Entregar 4 Frag + Redstone a los Prisioneros → Key");
        drawChecklistLine(graphics, x, y + 98, killed, "6. Vencer a EvilMario y reclamar Reliquias ROOT");
        graphics.drawString(this.font, "§7Controles: §e[R] §7Menú Habilidades  |  §b[V] §7Rayo Repulsor ADMIN-SQL", x, y + 120, 0xAAAAAA);
    }

    private void drawChecklistLine(GuiGraphics graphics, int x, int y, boolean done, String label) {
        String mark = done ? "§a[✓] " : "§7[ ] ";
        int color = done ? 0xAAFFAA : 0xCCCCCC;
        graphics.drawString(this.font, mark + label, x, y, color);
    }

    private void drawDialogueBox(GuiGraphics graphics, int x, int y, int width, int height, String rawText) {
        graphics.fill(x, y, x + width, y + height, 0xBB080A10);
        graphics.fill(x, y, x + width, y + 1, 0x6644AAFF);
        List<FormattedCharSequence> lines = this.font.split(Component.literal(rawText), width - 12);
        int maxLines = Math.max(1, (height - 8) / 10);
        for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
            graphics.drawString(this.font, lines.get(i), x + 6, y + 5 + i * 10, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int countItem(Player player, Item item) {
        if (player == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private int getRequirementAmount(int stage) {
        return switch (stage) {
            case 0 -> 5;
            case 1 -> 10;
            case 2 -> 15;
            case 3 -> 20;
            default -> 0;
        };
    }

    private String getChapterTitle(int stage) {
        return switch (stage) {
            case 0 -> "Ecos de la Línea 404";
            case 1 -> "El Escudo Cortafuegos";
            case 2 -> "Armas contra la Corrupción";
            case 3 -> "Rastreo de la Torre de Cristal";
            default -> "Completado";
        };
    }

    private String getRewardDescription(int stage) {
        return switch (stage) {
            case 0 -> "1 Fragmento + 5 XP + 4 Manzanas Doradas";
            case 1 -> "1 Fragmento + Set Armadura Cortafuegos";
            case 2 -> "1 Fragmento + Espada Glitch + Registro";
            case 3 -> "1 Fragmento + Mapas a la Torre Base de Datos";
            default -> "";
        };
    }
}
