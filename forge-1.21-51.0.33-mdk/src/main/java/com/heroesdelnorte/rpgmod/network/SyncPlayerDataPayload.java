package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public record SyncPlayerDataPayload(
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
    public SyncPlayerDataPayload(FriendlyByteBuf buffer) {
        this(
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readVarInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeUtf(this.characterClass);
        buffer.writeVarInt(this.bonusDamage);
        buffer.writeVarInt(this.bonusHealth);
        buffer.writeBoolean(this.openInitialSelection);
        buffer.writeVarInt(this.questStage);
        buffer.writeBoolean(this.questActive);
        buffer.writeBoolean(this.hasPyramid);
        buffer.writeInt(this.pyramidX);
        buffer.writeInt(this.pyramidY);
        buffer.writeInt(this.pyramidZ);
        buffer.writeBoolean(this.keyForged);
        buffer.writeBoolean(this.bossDefeated);
    }

    public static void handle(SyncPlayerDataPayload payload, CustomPayloadEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientHooks.handleSyncPlayerData(
                        payload.entityId(),
                        payload.characterClass(),
                        payload.bonusDamage(),
                        payload.bonusHealth(),
                        payload.openInitialSelection(),
                        payload.questStage(),
                        payload.questActive(),
                        payload.hasPyramid(),
                        payload.pyramidX(),
                        payload.pyramidY(),
                        payload.pyramidZ(),
                        payload.keyForged(),
                        payload.bossDefeated()
                )
        );
        context.setPacketHandled(true);
    }
}
