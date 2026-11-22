package com.heroesdelnorte.rpgmod.client;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.client.gui.NpcScreen;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.event.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only hooks invoked through {@link net.minecraftforge.fml.DistExecutor}
 * so common/server classes never directly load client classes.
 */
public class ClientHooks {

    public static void openNpcScreen(NpcBase npc) {
        Minecraft.getInstance().setScreen(new NpcScreen(npc));
    }

    public static void handleNpcDialogue(int npcEntityId, String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof NpcScreen npcScreen && npcScreen.getNpcId() == npcEntityId) {
            npcScreen.setDialogueResponse(text);
        }
    }

    public static void handleSyncPlayerData(
            int entityId,
            String characterClass,
            int bonusDamage,
            int bonusHealth,
            boolean openInitialSelection,
            int questStage,
            boolean questActive,
            boolean hasPyramid,
            int pyramidX,
            int pyramidY,
            int pyramidZ,
            boolean keyForged,
            boolean bossDefeated
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof Player target) {
            CompoundTag persistentData = target.getPersistentData();
            persistentData.putString(RpgStats.TAG_CLASS, characterClass);
            persistentData.putString(RpgStats.TAG_LEGACY_CLASS, characterClass);
            persistentData.putInt(RpgStats.TAG_BONUS_DAMAGE, bonusDamage);
            persistentData.putInt(RpgStats.TAG_BONUS_HEALTH, bonusHealth);
            persistentData.putInt(RpgStats.TAG_QUEST_STAGE, questStage);
            persistentData.putBoolean(RpgStats.TAG_QUEST_ACTIVE, questActive);
            persistentData.putBoolean(RpgStats.TAG_HAS_PYRAMID, hasPyramid);
            persistentData.putBoolean(RpgStats.TAG_KEY_FORGED, keyForged);
            persistentData.putBoolean(RpgStats.TAG_BOSS_DEFEATED, bossDefeated);
            if (hasPyramid) {
                persistentData.putInt(RpgStats.TAG_PYRAMID_X, pyramidX);
                persistentData.putInt(RpgStats.TAG_PYRAMID_Y, pyramidY);
                persistentData.putInt(RpgStats.TAG_PYRAMID_Z, pyramidZ);
            }
            RpgStats.setClientStats(target.getUUID(), bonusDamage, bonusHealth);
            RpgStats.setClientQuestData(target.getUUID(), questStage, questActive, hasPyramid, pyramidX, pyramidY, pyramidZ, keyForged, bossDefeated);
        }

        if (mc.player != null && mc.player.getId() == entityId) {
            if (openInitialSelection && characterClass.isEmpty()) {
                ClientEvents.requestInitialSelection();
            } else if (!characterClass.isEmpty()) {
                ClientEvents.clearInitialSelection();
            }
            if (mc.screen instanceof NpcScreen npcScreen) {
                npcScreen.refreshState();
            }
        }
    }
}
