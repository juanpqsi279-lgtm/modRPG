package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.event.CombatHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record FireRepulsorPayload() {

    public FireRepulsorPayload(FriendlyByteBuf buffer) {
        this();
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public static void handle(FireRepulsorPayload payload, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            CombatHandler.fireRepulsorBeam(player);
        }
        context.setPacketHandled(true);
    }
}
