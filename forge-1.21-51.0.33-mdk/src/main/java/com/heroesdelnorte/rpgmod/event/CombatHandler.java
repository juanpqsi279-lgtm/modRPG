package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.entity.EvilMinion;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.item.RpgArmorItem;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "rpgmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatHandler {

    private static final Map<UUID, Integer> hitCounters = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> repulsorCooldowns = new ConcurrentHashMap<>();

    public static void clearPlayer(UUID playerId) {
        hitCounters.remove(playerId);
        repulsorCooldowns.remove(playerId);
    }

    // Bonos pasivos de set completo (ADMIN-SQL, Cuántico y Cortafuegos) + Vuelo tipo Iron Man
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        boolean wearingAdmin = RpgArmorItem.hasFullAdminSqlSet(player);
        boolean hadFlight = player.getPersistentData().getBoolean(RpgStats.TAG_ADMIN_FLIGHT);

        if (wearingAdmin) {
            if (!hadFlight || !player.getAbilities().mayfly || player.getAbilities().getFlyingSpeed() < 0.08F) {
                player.getAbilities().mayfly = true;
                player.getAbilities().setFlyingSpeed(0.085F);
                player.getPersistentData().putBoolean(RpgStats.TAG_ADMIN_FLIGHT, true);
                player.onUpdateAbilities();
            }
            if (player.getAbilities().flying && player.tickCount % 2 == 0 && player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.15D, 0.05D, 0.15D, 0.02D);
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 0.1D, player.getZ(), 2, 0.20D, 0.05D, 0.20D, 0.03D);
            }
        } else if (hadFlight) {
            player.getPersistentData().putBoolean(RpgStats.TAG_ADMIN_FLIGHT, false);
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(0.05F);
                player.onUpdateAbilities();
            }
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (wearingAdmin) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false));
            if (player.getHealth() < player.getMaxHealth() && !player.hasEffect(MobEffects.REGENERATION)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false));
            }
        } else if (RpgArmorItem.hasFullQuantumSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1, true, false));
            if (player.getHealth() < player.getMaxHealth() && !player.hasEffect(MobEffects.REGENERATION)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
            }
        } else if (RpgArmorItem.hasFullFirewallSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
        }
    }

    // Rayo Repulsor SQL del Exotraje ADMIN-SQL MK.I
    public static void fireRepulsorBeam(ServerPlayer player) {
        if (!RpgArmorItem.hasFullAdminSqlSet(player)) {
            player.displayClientMessage(Component.literal("§c[ADMIN-SQL] Equipa el Exotraje ADMIN-SQL completo para usar el Rayo Repulsor."), true);
            return;
        }

        ServerLevel serverLevel = player.serverLevel();
        long now = serverLevel.getGameTime();
        long lastFired = repulsorCooldowns.getOrDefault(player.getUUID(), 0L);
        if (now - lastFired < 25L) {
            double remainingSec = Math.ceil((25L - (now - lastFired)) / 2.0D) / 10.0D;
            player.displayClientMessage(Component.literal("§b[Repulsor SQL] Recargando (" + remainingSec + "s)..."), true);
            return;
        }
        repulsorCooldowns.put(player.getUUID(), now);

        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.6D));
        Vec3 maxEnd = start.add(look.scale(32.0D));

        HitResult blockHit = serverLevel.clip(new ClipContext(start, maxEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDist = start.distanceTo(blockHit.getLocation());
        Vec3 impactPos = blockHit.getLocation();

        for (double d = 0.5D; d <= maxDist; d += 0.5D) {
            Vec3 point = start.add(look.scale(d));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 2, 0.04D, 0.04D, 0.04D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, point.x, point.y, point.z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
            if (((int) Math.round(d * 2.0D)) % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            AABB checkBox = new AABB(point.x - 0.8D, point.y - 0.8D, point.z - 0.8D,
                    point.x + 0.8D, point.y + 0.8D, point.z + 0.8D);
            List<LivingEntity> hitEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, checkBox,
                    entity -> entity != player && !(entity instanceof NpcBase) && entity.isAlive());
            if (!hitEntities.isEmpty()) {
                impactPos = point;
                break;
            }
        }

        serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, impactPos.x, impactPos.y, impactPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactPos.x, impactPos.y, impactPos.z, 20, 0.8D, 0.8D, 0.8D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, impactPos.x, impactPos.y, impactPos.z, 12, 0.6D, 0.6D, 0.6D, 0.05D);

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.65F, 1.7F);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.8F);

        AABB impactArea = new AABB(impactPos.x - 2.6D, impactPos.y - 2.6D, impactPos.z - 2.6D,
                impactPos.x + 2.6D, impactPos.y + 2.6D, impactPos.z + 2.6D);
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, impactArea,
                entity -> entity != player && !(entity instanceof NpcBase) && entity.isAlive());
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), 20.0F);
            target.igniteForSeconds(3.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            target.push(look.x * 1.1D, 0.35D, look.z * 1.1D);
        }

        player.displayClientMessage(Component.literal("§b§l[RAYO REPULSOR SQL] §fSELECT * FROM targets WHERE hp = 0;"), true);
    }

    // Inmunidad a Caídas, Rayos y Fuego con ADMIN-SQL; Rayos y Fuego con Núcleo Cuántico
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            if (RpgArmorItem.hasFullAdminSqlSet(player)
                    && (event.getSource().is(DamageTypeTags.IS_LIGHTNING)
                    || event.getSource().is(DamageTypeTags.IS_FIRE)
                    || event.getSource().is(DamageTypeTags.IS_FALL))) {
                event.setCanceled(true);
                player.clearFire();
            } else if (RpgArmorItem.hasFullQuantumSet(player)
                    && (event.getSource().is(DamageTypeTags.IS_LIGHTNING) || event.getSource().is(DamageTypeTags.IS_FIRE))) {
                event.setCanceled(true);
                player.clearFire();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // Reducción adicional de daño contra EvilMario y Secuaces Corruptos
        if (event.getEntity() instanceof Player victim && !victim.level().isClientSide()) {
            if (event.getSource().getEntity() instanceof EvilMario || event.getSource().getEntity() instanceof EvilMinion) {
                if (RpgArmorItem.hasFullAdminSqlSet(victim)) {
                    event.setAmount(event.getAmount() * 0.65F);
                } else if (RpgArmorItem.hasFullQuantumSet(victim)) {
                    event.setAmount(event.getAmount() * 0.75F);
                }
            }
        }

        if (event.getSource().getEntity() instanceof Player player && event.getEntity() != null) {
            LivingEntity target = event.getEntity();
            if (player.level().isClientSide() || target == player) {
                return;
            }

            ItemStack heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof RpgWeapon)) {
                return;
            }

            UUID playerId = player.getUUID();
            // +5 de daño por cada punto de habilidad comprado en el menú
            int bonusDmg = RpgStats.getBonusDamage(player) * 5;
            // Conserva bonificadores de atributos/pociones (ej. +4 pasivo de Josepe o Fuerza II)
            float extraAttrDamage = Math.max(0.0f, event.getAmount() - 1.0f);
            int currentHits = hitCounters.getOrDefault(playerId, 0) + 1;

            // 1. LLAVE INGLESA (URIEL): 12 base (+ daño extra). Al 3er golpe: 25 base
            if (heldItem.is(ModItems.WRENCH.get())) {
                if (currentHits >= 3) {
                    float totalDamage = 25.0f + bonusDmg + extraAttrDamage;
                    event.setAmount(totalDamage);
                    player.sendSystemMessage(Component.literal("§6[Uriel] ¡SUPER GOLPE CARGADO! (" + Math.round(totalDamage) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    float totalDamage = 12.0f + bonusDmg + extraAttrDamage;
                    event.setAmount(totalDamage);
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§6[Uriel] §7Golpe " + currentHits + "/3 (" + Math.round(totalDamage) + " de Daño)"));
                }
            }
            // 2. LÁTIGO (CHATGPT): 14 base (+ daño extra). Al 3er golpe: Velocidad
            else if (heldItem.is(ModItems.WHIP.get())) {
                float totalDamage = 14.0f + bonusDmg + extraAttrDamage;
                event.setAmount(totalDamage);
                if (currentHits >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 1));
                    player.sendSystemMessage(Component.literal("§b[ChatGPT] ¡Velocidad activada por 30s! (" + Math.round(totalDamage) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§b[ChatGPT] §7Golpe " + currentHits + "/3 (" + Math.round(totalDamage) + " de Daño)"));
                }
            }
            // 3. HACHA (JOSEPE): 15 base (+ daño extra). Al 3er golpe: Invisibilidad y Fuerza
            else if (heldItem.is(ModItems.JOSEPE_AXE.get())) {
                float totalDamage = 15.0f + bonusDmg + extraAttrDamage;
                event.setAmount(totalDamage);
                if (currentHits >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 25, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 25, 1)); // Fuerza II
                    player.sendSystemMessage(Component.literal("§c[Josepe] ¡Fuerza e Invisibilidad por 25s! (" + Math.round(totalDamage) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§c[Josepe] §7Golpe " + currentHits + "/3 (" + Math.round(totalDamage) + " de Daño)"));
                }
            }
        }
    }

    // Cancelación estricta de soltar armas RPG (tecla Q)
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (stack.getItem() instanceof RpgWeapon) {
            event.setCanceled(true);

            Player player = event.getPlayer();
            if (!player.getInventory().contains(stack)) {
                player.getInventory().add(stack);
            }

            player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
            player.sendSystemMessage(Component.literal("§c[!] Tu arma RPG es fija y no se puede tirar."));
        }
    }

    // Sincroniza la mano al recoger ítems con arma RPG equipada
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() instanceof RpgWeapon) {
            player.containerMenu.broadcastChanges();
        }
    }
}
