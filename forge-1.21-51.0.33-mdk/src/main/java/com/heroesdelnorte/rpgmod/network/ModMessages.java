package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.RpgStats;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath("rpgmod", "main"))
                .networkProtocolVersion(1)
                .simpleChannel();

        INSTANCE.messageBuilder(SelectCharacterPayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectCharacterPayload::write)
                .decoder(SelectCharacterPayload::new)
                .consumerMainThread(SelectCharacterPayload::handle)
                .add();

        INSTANCE.messageBuilder(UpgradeSkillPayload.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpgradeSkillPayload::write)
                .decoder(UpgradeSkillPayload::new)
                .consumerMainThread(UpgradeSkillPayload::handle)
                .add();

        INSTANCE.messageBuilder(SyncPlayerDataPayload.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncPlayerDataPayload::write)
                .decoder(SyncPlayerDataPayload::new)
                .consumerMainThread(SyncPlayerDataPayload::handle)
                .add();

        INSTANCE.messageBuilder(NpcActionPayload.class, 3, NetworkDirection.PLAY_TO_SERVER)
                .encoder(NpcActionPayload::write)
                .decoder(NpcActionPayload::new)
                .consumerMainThread(NpcActionPayload::handle)
                .add();

        INSTANCE.messageBuilder(FireRepulsorPayload.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FireRepulsorPayload::write)
                .decoder(FireRepulsorPayload::new)
                .consumerMainThread(FireRepulsorPayload::handle)
                .add();

        INSTANCE.messageBuilder(NpcDialoguePayload.class, 5, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(NpcDialoguePayload::write)
                .decoder(NpcDialoguePayload::new)
                .consumerMainThread(NpcDialoguePayload::handle)
                .add();

        INSTANCE.build();
    }

    public static void sendToServer(Object message) {
        if (INSTANCE != null) {
            INSTANCE.send(message, PacketDistributor.SERVER.noArg());
        }
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        if (INSTANCE != null && player != null) {
            INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
        }
    }

    public static void sendToTrackingAndSelf(Object message, ServerPlayer player) {
        if (INSTANCE != null && player != null) {
            INSTANCE.send(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
        }
    }

    private static SyncPlayerDataPayload buildSyncPayload(ServerPlayer player, boolean openInitialSelection) {
        boolean hasPyramid = RpgStats.hasPyramidCoords(player);
        BlockPos pyramidPos = hasPyramid ? RpgStats.getPyramidPos(player) : null;
        return new SyncPlayerDataPayload(
                player.getId(),
                RpgStats.getActiveClass(player),
                RpgStats.getBonusDamage(player),
                RpgStats.getBonusHealth(player),
                openInitialSelection,
                RpgStats.getQuestStage(player),
                RpgStats.isQuestActive(player),
                pyramidPos != null,
                pyramidPos != null ? pyramidPos.getX() : 0,
                pyramidPos != null ? pyramidPos.getY() : 0,
                pyramidPos != null ? pyramidPos.getZ() : 0,
                RpgStats.isKeyForged(player),
                RpgStats.isBossDefeated(player)
        );
    }

    public static void syncPlayerToSelf(ServerPlayer player, boolean openInitialSelection) {
        sendToPlayer(buildSyncPayload(player, openInitialSelection), player);
    }

    public static void syncPlayerToTrackingAndSelf(ServerPlayer player, boolean openInitialSelection) {
        sendToTrackingAndSelf(buildSyncPayload(player, openInitialSelection), player);
    }
}
