package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record NpcActionPayload(int npcEntityId, int actionId, int param) {

    public NpcActionPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.npcEntityId);
        buffer.writeVarInt(this.actionId);
        buffer.writeVarInt(this.param);
    }

    public static void handle(NpcActionPayload payload, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            Entity entity = player.serverLevel().getEntity(payload.npcEntityId());
            if (entity instanceof NpcBase npc && player.distanceToSqr(npc) <= 64.0D) {
                npc.handleMenuAction(player, payload.actionId(), payload.param());
            }
        }
        context.setPacketHandled(true);
    }
}
