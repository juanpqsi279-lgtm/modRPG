package com.heroesdelnorte.rpgmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class GlitchSwordItem extends SwordItem {

    public GlitchSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    // Este método detecta cuando el jugador da Clic Derecho al aire o a un bloque
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Trazamos una línea invisible desde los ojos del jugador para ver a dónde apunta
            HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos targetPos = ((BlockHitResult) hitResult).getBlockPos();

                // Invocar el Rayo Destructor en las coordenadas a las que mira
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                    level.addFreshEntity(lightning);
                }

                // Reproducir un sonido épico (el de cuando se abre el portal del End)
                level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 1.0f);

                // Anuncio en la barra de acción
                player.displayClientMessage(Component.literal("§d[!] ¡Habilidad Glitch activada!"), true);

                // ¡AQUÍ ESTÁ LA MAGIA DEL RPG! Agregar un cooldown de 10 segundos (200 ticks)
                player.getCooldowns().addCooldown(this, 200);

                // Gastar un poco de durabilidad del arma por usar la magia
                stack.hurtAndBreak(5, player, player.getEquipmentSlotForItem(stack));
            }
        }

        // Indicamos que la acción fue un éxito
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}