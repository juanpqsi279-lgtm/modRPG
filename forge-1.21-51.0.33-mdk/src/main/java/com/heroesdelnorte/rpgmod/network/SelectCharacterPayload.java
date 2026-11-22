package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.event.CharacterInventoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record SelectCharacterPayload(String characterName) implements CustomPacketPayload {

    public static final Type<SelectCharacterPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rpgmod", "select_character"));

    public static final StreamCodec<FriendlyByteBuf, SelectCharacterPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SelectCharacterPayload::write, SelectCharacterPayload::new);

    public SelectCharacterPayload(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.characterName);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectCharacterPayload payload, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && RpgStats.isValidClass(payload.characterName())) {
            String previousClass = RpgStats.getActiveClass(player);

            if (!payload.characterName().equals(previousClass)) {
                CharacterInventoryManager.switchCharacterInventory(player, previousClass, payload.characterName());
                ModMessages.syncPlayerToTrackingAndSelf(player, false);
            }
        }
        context.setPacketHandled(true);
    }
}