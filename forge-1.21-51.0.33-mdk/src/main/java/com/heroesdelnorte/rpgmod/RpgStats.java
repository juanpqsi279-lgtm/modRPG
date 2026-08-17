package com.heroesdelnorte.rpgmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RpgStats {
    // Guarda el nivel de daño extra comprado por cada jugador (+5 de daño por nivel)
    public static final Map<UUID, Integer> BONUS_DAMAGE = new HashMap<>();

    // Guarda la salud extra comprada (+4 HP = 2 corazones por nivel)
    public static final Map<UUID, Integer> BONUS_HEALTH = new HashMap<>();

    public static int getBonusDamage(UUID playerId) {
        return BONUS_DAMAGE.getOrDefault(playerId, 0);
    }

    public static void addBonusDamage(UUID playerId) {
        BONUS_DAMAGE.put(playerId, getBonusDamage(playerId) + 1);
    }

    public static int getBonusHealth(UUID playerId) {
        return BONUS_HEALTH.getOrDefault(playerId, 0);
    }

    public static void addBonusHealth(UUID playerId) {
        BONUS_HEALTH.put(playerId, getBonusHealth(playerId) + 1);
    }
}