package com.heroesdelnorte.rpgmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ModMessages {

    private static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath("rpgmod", "main"))
                .networkProtocolVersion(1)
                .simpleChannel();

        INSTANCE.messageBuilder(SelectCharacterPayload.class)
                .encoder(SelectCharacterPayload::write)
                .decoder(SelectCharacterPayload::new)
                .consumerNetworkThread(SelectCharacterPayload::handle)
                .add();
    }

    public static void sendToServer(Object message) {
        if (INSTANCE != null) {
            INSTANCE.send(message, PacketDistributor.SERVER.noArg());
        }
    }
}