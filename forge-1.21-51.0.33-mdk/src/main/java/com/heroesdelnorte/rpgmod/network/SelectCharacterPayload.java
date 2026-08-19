package com.heroesdelnorte.rpgmod.network;

import com.heroesdelnorte.rpgmod.event.CharacterInventoryManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                CompoundTag persistentData = player.getPersistentData();
                String previousClass = persistentData.getString("RpgModClass");

                if (!payload.characterName().equals(previousClass)) {
                    // EJECUTA EL CAMBIO DE INVENTARIO
                    CharacterInventoryManager.switchCharacterInventory(player, previousClass, payload.characterName());
                    persistentData.putString("RpgModClass", payload.characterName());

                    // --- ¡INYECCIÓN DEL SISTEMA DE CLASES RPG! ---
                    applyCharacterStats(player, payload.characterName());
                }
            }
        });
        context.setPacketHandled(true);
    }

    // ==========================================
    // SISTEMA DE ATRIBUTOS (Fase 1)
    // ==========================================

    // Identificadores únicos para que Minecraft no confunda nuestros modificadores con los de las pociones o armaduras
    private static final ResourceLocation HEALTH_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_health");
    private static final ResourceLocation SPEED_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_speed");
    private static final ResourceLocation DAMAGE_MOD_ID = ResourceLocation.fromNamespaceAndPath("rpgmod", "class_damage");

    private static void applyCharacterStats(ServerPlayer player, String characterName) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);

        // 1. Limpiar modificadores anteriores para que las estadísticas no se sumen al infinito al cambiar de personaje
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_MOD_ID);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MOD_ID);
        if (damageAttr != null) damageAttr.removeModifier(DAMAGE_MOD_ID);

        // 2. Aplicar la pasiva única según el personaje seleccionado
        switch (characterName.toLowerCase()) {
            case "uriel":
                // El Tanque: +20 de Vida Máxima (Te da 2 filas enteras de corazones)
                if (healthAttr != null) {
                    healthAttr.addPermanentModifier(new AttributeModifier(HEALTH_MOD_ID, 20.0, AttributeModifier.Operation.ADD_VALUE));
                }
                player.heal(20.0F); // Curar instantáneamente para llenar esos nuevos corazones
                break;

            case "josepe":
                // El DPS: +4 de Daño Base (Como traer una espada de hierro invisible en los puños)
                if (damageAttr != null) {
                    damageAttr.addPermanentModifier(new AttributeModifier(DAMAGE_MOD_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
                }
                break;

            case "chatgpt":
                // El Veloz: +0.05 a la velocidad base (Eres súper rápido)
                if (speedAttr != null) {
                    speedAttr.addPermanentModifier(new AttributeModifier(SPEED_MOD_ID, 0.05, AttributeModifier.Operation.ADD_VALUE));
                }
                break;
        }

        // 3. Reajustar la vida si al quitar el modo tanque el jugador tiene más vida de la que debería
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }
}