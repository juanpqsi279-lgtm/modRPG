package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.RpgStats;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record UpgradeSkillPayload(String skill) {

    public UpgradeSkillPayload(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.skill);
    }

    public static void handle(UpgradeSkillPayload payload, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            String skill = payload.skill();
            if (!"daño".equals(skill) && !"salud".equals(skill)) {
                context.setPacketHandled(true);
                return;
            }

            if (player.experienceLevel <= 0) {
                player.sendSystemMessage(Component.literal("§c¡No tienes suficiente XP/Puntos!"));
                context.setPacketHandled(true);
                return;
            }

            player.giveExperienceLevels(-1);
            if ("daño".equals(skill)) {
                RpgStats.addBonusDamage(player);
                player.sendSystemMessage(Component.literal("§c⚔ ¡Daño aumentado! (+5 extra)"));
            } else {
                RpgStats.addBonusHealth(player);
                RpgStats.applyAllPlayerAttributes(player);
                player.heal(4.0F);
                player.sendSystemMessage(Component.literal("§a❤ ¡Salud aumentada! (+2 corazones)"));
            }

            ModMessages.syncPlayerToSelf(player, false);
        }
        context.setPacketHandled(true);
    }
}

