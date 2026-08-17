package com.heroesdelnorte.rpgmod.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class NpcBase extends Mob {
    // Variable sincronizada que define qué skin usará el NPC al generarse
    private static final EntityDataAccessor<Integer> SKIN_ID = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);

    public NpcBase(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_ID, this.random.nextInt(15)); // Elige un número del 0 al 14
    }

    // Método para que el Renderer sepa qué textura ponerle
    public int getSkinId() {
        return this.entityData.get(SKIN_ID);
    }

    // Atributos base del NPC
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D); // En cero para que se queden en su lugar
    }

    // Evento de interacción (Diálogo al hacer clic derecho)
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && hand == InteractionHand.MAIN_HAND) {
            player.sendSystemMessage(Component.literal("§eAldeano: §f¡Apresúrate! EvilMario está destruyendo el servidor..."));
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}