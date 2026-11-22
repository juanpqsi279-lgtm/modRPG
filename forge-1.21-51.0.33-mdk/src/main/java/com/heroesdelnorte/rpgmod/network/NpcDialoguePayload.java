package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public record NpcDialoguePayload(int npcEntityId, String text) {

    public NpcDialoguePayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUtf(2048));
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.npcEntityId);
        buffer.writeUtf(this.text != null ? this.text : "", 2048);
    }

    public static void handle(NpcDialoguePayload payload, CustomPayloadEvent.Context context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientHooks.handleNpcDialogue(payload.npcEntityId(), payload.text())
        );
        context.setPacketHandled(true);
    }
}
