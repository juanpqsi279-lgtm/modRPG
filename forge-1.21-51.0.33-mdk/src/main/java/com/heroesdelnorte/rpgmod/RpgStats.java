package com.heroesdelnorte.rpgmod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RpgStats {

    public static final String TAG_CLASS = "RpgModClass";
    public static final String TAG_LEGACY_CLASS = "ActiveRPGClass";
    public static final String TAG_BONUS_DAMAGE = "RpgBonusDamage";
    public static final String TAG_BONUS_HEALTH = "RpgBonusHealth";
    public static final String TAG_QUEST_STAGE = "RpgQuestStage";
    public static final String TAG_QUEST_ACTIVE = "RpgQuestActive";
    public static final String TAG_HAS_PYRAMID = "RpgHasPyramidCoords";
    public static final String TAG_PYRAMID_X = "RpgPyramidX";
    public static final String TAG_PYRAMID_Y = "RpgPyramidY";
    public static final String TAG_PYRAMID_Z = "RpgPyramidZ";
    public static final String TAG_KEY_FORGED = "RpgKeyForged";
    public static final String TAG_BOSS_DEFEATED = "RpgBossDefeated";
    public static final String TAG_ADMIN_FLIGHT = "RpgAdminFlightActive";

    public static final ResourceLocation CLASS_HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_health");
    public static final ResourceLocation CLASS_SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_speed");
    public static final ResourceLocation CLASS_DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_damage");
    public static final ResourceLocation SKILL_HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "skill_health");

    // Caché en memoria (respaldada en NBT persistente del jugador)
    public static final Map<UUID, Integer> BONUS_DAMAGE = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> BONUS_HEALTH = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> QUEST_STAGE = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> QUEST_ACTIVE = new ConcurrentHashMap<>();
    public static final Map<UUID, BlockPos> PYRAMID_POS = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> KEY_FORGED = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> BOSS_DEFEATED = new ConcurrentHashMap<>();

    public static boolean isValidClass(String characterName) {
        return "Uriel".equals(characterName)
                || "ChatGPT".equals(characterName)
                || "Josepe".equals(characterName);
    }

    public static String getActiveClass(Player player) {
        if (player == null) {
            return "";
        }
        CompoundTag data = player.getPersistentData();
        String cls = data.getString(TAG_CLASS);
        if (cls.isEmpty()) {
            cls = data.getString(TAG_LEGACY_CLASS);
        }
        return isValidClass(cls) ? cls : "";
    }

    public static void setActiveClass(Player player, String characterClass) {
        if (player == null) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        data.putString(TAG_CLASS, characterClass);
        data.putString(TAG_LEGACY_CLASS, characterClass);
    }

    public static int getBonusDamage(Player player) {
        if (player == null) {
            return 0;
        }
        if (player.level().isClientSide()) {
            return BONUS_DAMAGE.getOrDefault(player.getUUID(), Math.max(0, player.getPersistentData().getInt(TAG_BONUS_DAMAGE)));
        }
        int val = Math.max(0, player.getPersistentData().getInt(TAG_BONUS_DAMAGE));
        BONUS_DAMAGE.put(player.getUUID(), val);
        return val;
    }

    public static void addBonusDamage(Player player) {
        if (player == null) {
            return;
        }
        int next = getBonusDamage(player) + 1;
        player.getPersistentData().putInt(TAG_BONUS_DAMAGE, next);
        BONUS_DAMAGE.put(player.getUUID(), next);
    }

    public static int getBonusHealth(Player player) {
        if (player == null) {
            return 0;
        }
        if (player.level().isClientSide()) {
            return BONUS_HEALTH.getOrDefault(player.getUUID(), Math.max(0, player.getPersistentData().getInt(TAG_BONUS_HEALTH)));
        }
        int val = Math.max(0, player.getPersistentData().getInt(TAG_BONUS_HEALTH));
        BONUS_HEALTH.put(player.getUUID(), val);
        return val;
    }

    public static void addBonusHealth(Player player) {
        if (player == null) {
            return;
        }
        int next = getBonusHealth(player) + 1;
        player.getPersistentData().putInt(TAG_BONUS_HEALTH, next);
        BONUS_HEALTH.put(player.getUUID(), next);
    }

    public static int getQuestStage(Player player) {
        if (player == null) {
            return 0;
        }
        if (player.level().isClientSide()) {
            return QUEST_STAGE.getOrDefault(player.getUUID(), Math.max(0, player.getPersistentData().getInt(TAG_QUEST_STAGE)));
        }
        int stage = Math.max(0, player.getPersistentData().getInt(TAG_QUEST_STAGE));
        QUEST_STAGE.put(player.getUUID(), stage);
        return stage;
    }

    public static void setQuestStage(Player player, int stage) {
        if (player == null) {
            return;
        }
        int clamped = Math.max(0, stage);
        player.getPersistentData().putInt(TAG_QUEST_STAGE, clamped);
        QUEST_STAGE.put(player.getUUID(), clamped);
    }

    public static boolean isQuestActive(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return QUEST_ACTIVE.getOrDefault(player.getUUID(), player.getPersistentData().getBoolean(TAG_QUEST_ACTIVE));
        }
        boolean active = player.getPersistentData().getBoolean(TAG_QUEST_ACTIVE);
        QUEST_ACTIVE.put(player.getUUID(), active);
        return active;
    }

    public static void setQuestActive(Player player, boolean active) {
        if (player == null) {
            return;
        }
        player.getPersistentData().putBoolean(TAG_QUEST_ACTIVE, active);
        QUEST_ACTIVE.put(player.getUUID(), active);
    }

    public static boolean hasPyramidCoords(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return PYRAMID_POS.containsKey(player.getUUID()) || player.getPersistentData().getBoolean(TAG_HAS_PYRAMID);
        }
        return player.getPersistentData().getBoolean(TAG_HAS_PYRAMID);
    }

    public static BlockPos getPyramidPos(Player player) {
        if (player == null || !hasPyramidCoords(player)) {
            return null;
        }
        if (player.level().isClientSide() && PYRAMID_POS.containsKey(player.getUUID())) {
            return PYRAMID_POS.get(player.getUUID());
        }
        CompoundTag data = player.getPersistentData();
        BlockPos pos = new BlockPos(data.getInt(TAG_PYRAMID_X), data.getInt(TAG_PYRAMID_Y), data.getInt(TAG_PYRAMID_Z));
        PYRAMID_POS.put(player.getUUID(), pos);
        return pos;
    }

    public static void setPyramidPos(Player player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_HAS_PYRAMID, true);
        data.putInt(TAG_PYRAMID_X, pos.getX());
        data.putInt(TAG_PYRAMID_Y, pos.getY());
        data.putInt(TAG_PYRAMID_Z, pos.getZ());
        PYRAMID_POS.put(player.getUUID(), pos);
    }

    public static boolean isKeyForged(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return KEY_FORGED.getOrDefault(player.getUUID(), player.getPersistentData().getBoolean(TAG_KEY_FORGED));
        }
        boolean forged = player.getPersistentData().getBoolean(TAG_KEY_FORGED);
        KEY_FORGED.put(player.getUUID(), forged);
        return forged;
    }

    public static void setKeyForged(Player player, boolean forged) {
        if (player == null) {
            return;
        }
        player.getPersistentData().putBoolean(TAG_KEY_FORGED, forged);
        KEY_FORGED.put(player.getUUID(), forged);
    }

    public static boolean isBossDefeated(Player player) {
        if (player == null) {
            return false;
        }
        if (player.level().isClientSide()) {
            return BOSS_DEFEATED.getOrDefault(player.getUUID(), player.getPersistentData().getBoolean(TAG_BOSS_DEFEATED));
        }
        boolean defeated = player.getPersistentData().getBoolean(TAG_BOSS_DEFEATED);
        BOSS_DEFEATED.put(player.getUUID(), defeated);
        return defeated;
    }

    public static void setBossDefeated(Player player, boolean defeated) {
        if (player == null) {
            return;
        }
        player.getPersistentData().putBoolean(TAG_BOSS_DEFEATED, defeated);
        BOSS_DEFEATED.put(player.getUUID(), defeated);
    }

    public static int getBonusDamage(UUID playerId) {
        return BONUS_DAMAGE.getOrDefault(playerId, 0);
    }

    public static int getBonusHealth(UUID playerId) {
        return BONUS_HEALTH.getOrDefault(playerId, 0);
    }

    public static void setClientStats(UUID playerId, int bonusDamage, int bonusHealth) {
        BONUS_DAMAGE.put(playerId, Math.max(0, bonusDamage));
        BONUS_HEALTH.put(playerId, Math.max(0, bonusHealth));
    }

    public static void setClientQuestData(UUID playerId, int questStage, boolean questActive, boolean hasPyramid, int x, int y, int z, boolean keyForged, boolean bossDefeated) {
        QUEST_STAGE.put(playerId, Math.max(0, questStage));
        QUEST_ACTIVE.put(playerId, questActive);
        if (hasPyramid) {
            PYRAMID_POS.put(playerId, new BlockPos(x, y, z));
        } else {
            PYRAMID_POS.remove(playerId);
        }
        KEY_FORGED.put(playerId, keyForged);
        BOSS_DEFEATED.put(playerId, bossDefeated);
    }

    public static void clearPlayer(UUID playerId) {
        BONUS_DAMAGE.remove(playerId);
        BONUS_HEALTH.remove(playerId);
        QUEST_STAGE.remove(playerId);
        QUEST_ACTIVE.remove(playerId);
        PYRAMID_POS.remove(playerId);
        KEY_FORGED.remove(playerId);
        BOSS_DEFEATED.remove(playerId);
    }

    /**
     * Aplica de forma idempotente los modificadores de atributos de la clase activa
     * y los puntos de salud comprados con experiencia.
     */
    public static void applyAllPlayerAttributes(ServerPlayer player) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (healthAttr != null) {
            healthAttr.removeModifier(CLASS_HEALTH_MOD_ID);
            healthAttr.removeModifier(SKILL_HEALTH_MOD_ID);
        }
        if (speedAttr != null) {
            speedAttr.removeModifier(CLASS_SPEED_MOD_ID);
        }
        if (damageAttr != null) {
            damageAttr.removeModifier(CLASS_DAMAGE_MOD_ID);
        }

        String activeClass = getActiveClass(player);
        switch (activeClass.toLowerCase(Locale.ROOT)) {
            case "uriel":
                if (healthAttr != null) {
                    healthAttr.addPermanentModifier(new AttributeModifier(CLASS_HEALTH_MOD_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
                }
                break;
            case "josepe":
                if (damageAttr != null) {
                    damageAttr.addPermanentModifier(new AttributeModifier(CLASS_DAMAGE_MOD_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
                }
                break;
            case "chatgpt":
                if (speedAttr != null) {
                    speedAttr.addPermanentModifier(new AttributeModifier(CLASS_SPEED_MOD_ID, 0.05, AttributeModifier.Operation.ADD_VALUE));
                }
                break;
            default:
                break;
        }

        int bonusHealth = getBonusHealth(player);
        if (bonusHealth > 0 && healthAttr != null) {
            healthAttr.addPermanentModifier(new AttributeModifier(SKILL_HEALTH_MOD_ID, bonusHealth * 4.0, AttributeModifier.Operation.ADD_VALUE));
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}