package com.heroesdelnorte.rpgmod.item;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DropTableSwordItem extends SwordItem {

    public DropTableSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        attacker.heal(2.0F);

        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 0.5D, target.getZ(), 4, 0.25D, 0.25D, 0.25D, 0.03D);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 maxPos = eyePos.add(look.scale(18.0D));

            HitResult hit = serverLevel.clip(new ClipContext(eyePos, maxPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 impactPos = hit.getLocation();

            double beamLength = eyePos.distanceTo(impactPos);
            for (double d = 0.6D; d <= beamLength; d += 0.6D) {
                Vec3 point = eyePos.add(look.scale(d));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
                if (((int) Math.round(d * 2.0D)) % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }

            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightning != null) {
                lightning.moveTo(impactPos.x, impactPos.y, impactPos.z);
                lightning.setVisualOnly(true);
                if (player instanceof ServerPlayer sp) {
                    lightning.setCause(sp);
                }
                serverLevel.addFreshEntity(lightning);
            }

            AABB area = new AABB(impactPos.x - 7.0D, impactPos.y - 3.5D, impactPos.z - 7.0D,
                    impactPos.x + 7.0D, impactPos.y + 4.5D, impactPos.z + 7.0D);
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> entity != player && !(entity instanceof NpcBase) && entity.isAlive());

            for (LivingEntity target : targets) {
                target.hurt(player.damageSources().playerAttack(player), 24.0F);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                Vec3 push = target.position().subtract(impactPos);
                if (push.lengthSqr() < 0.01D) {
                    push = look;
                } else {
                    push = push.normalize();
                }
                target.push(push.x * 0.9D, 0.45D, push.z * 0.9D);
            }

            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, impactPos.x, impactPos.y + 0.5D, impactPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactPos.x, impactPos.y + 0.5D, impactPos.z, 35, 2.2D, 0.8D, 2.2D, 0.15D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, impactPos.x, impactPos.y + 0.4D, impactPos.z, 20, 1.8D, 0.5D, 1.8D, 0.06D);

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.2F);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);

            player.displayClientMessage(Component.literal("§c§l[DROP TABLE;] §b¡Purga de datos ejecutada!"), true);
            player.getCooldowns().addCooldown(this, 100);
            stack.hurtAndBreak(2, player, LivingEntity.getSlotForHand(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("§4§l✦ Reliquia ROOT: Espada DROP TABLE;"));
        tooltipComponents.add(Component.literal("§6Exclusiva de EvilMario (No crafteable)"));
        tooltipComponents.add(Component.literal("§c  • Al golpear: Wither II, Lentitud II y Robo de Vida (+2 HP)"));
        tooltipComponents.add(Component.literal("§b  • Clic Derecho (5s CD): Rayo + Onda Purga SQL en área (24 daño)"));
    }
}
