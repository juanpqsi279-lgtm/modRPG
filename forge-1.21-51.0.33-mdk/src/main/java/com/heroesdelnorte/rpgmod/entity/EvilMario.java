package com.heroesdelnorte.rpgmod.entity;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EvilMario extends Monster {

    private final ServerBossEvent bossInfo = (ServerBossEvent) new ServerBossEvent(
            this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    ).setDarkenScreen(true);

    private int dialogueCooldown = 0;

    private static final String[] FRASES = {
            "Maldita IA",
            "Preguntaselo al chat",
            "Yo vengo a jalar",
            "Ando a raja tabla",
            "¡Yo soy el administrador ROOT de esta Base de Datos!",
            "¡Ningún prisionero saldrá jamás de mis tablas!"
    };

    public EvilMario(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setHealth(this.getMaxHealth());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // ATAQUE CON COOLDOWN DE 10 SEGUNDOS (200 ticks)
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            protected int getAttackInterval() {
                return 200; // 10 segundos entre cada golpe
            }
        });

        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D) // Daño fuerte
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (target instanceof Player && target.isAlive()) {
                dialogueCooldown++;
                if (dialogueCooldown >= 200) {
                    dialogueCooldown = 0;
                    shoutRandomPhrase();
                }
            } else {
                dialogueCooldown = 0;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);

        // Si el golpe fue exitoso, sigue vivo y estamos en el servidor
        if (wasHurt && !this.level().isClientSide() && this.isAlive() && this.getHealth() > 0.0F) {

            // FASE 2: Solo si su vida cae por debajo de la mitad
            if (this.getHealth() < (this.getMaxHealth() / 2.0F)) {

                // Probabilidad reducida al 15%
                if (this.random.nextFloat() < 0.15F) {

                    // SISTEMA ANTISPAM: cuenta cuántos EvilMinions hay en un radio de 10 bloques
                    long minionsCerca = this.level().getEntitiesOfClass(EvilMinion.class, this.getBoundingBox().inflate(10.0)).size();

                    // Solo invoca si hay menos de 3 vivos
                    if (minionsCerca < 3) {
                        this.level().playSound(null, this.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.HOSTILE, 1.0F, 1.0F);

                        EvilMinion minion = ModEntities.EVIL_MINION.get().create(this.level());
                        if (minion != null) {
                            double offsetX = (this.random.nextDouble() - 0.5) * 4.0;
                            double offsetZ = (this.random.nextDouble() - 0.5) * 4.0;
                            minion.moveTo(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);

                            this.level().addFreshEntity(minion);
                        }
                    }
                }
            }
        }
        return wasHurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Reliquias ROOT exclusivas de EvilMario (no crafteables ni en cofres)
        this.spawnAtLocation(new ItemStack(ModItems.DROP_TABLE_SWORD.get(), 1));
        this.spawnAtLocation(new ItemStack(ModItems.ADMIN_HELMET.get(), 1));
        this.spawnAtLocation(new ItemStack(ModItems.ADMIN_CHESTPLATE.get(), 1));
        this.spawnAtLocation(new ItemStack(ModItems.ADMIN_LEGGINGS.get(), 1));
        this.spawnAtLocation(new ItemStack(ModItems.ADMIN_BOOTS.get(), 1));

        // Botín adicional de jefe
        this.spawnAtLocation(new ItemStack(ModItems.CORRUPTED_BIT.get(), 16));
        this.spawnAtLocation(new ItemStack(Items.NETHER_STAR, 1));
        this.spawnAtLocation(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 2));

        // Registrar victoria en los jugadores cercanos y en el atacante final
        if (damageSource.getEntity() instanceof ServerPlayer killer) {
            RpgStats.setBossDefeated(killer, true);
            ModMessages.syncPlayerToSelf(killer, false);
        }
        List<ServerPlayer> nearby = level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(48.0D));
        for (ServerPlayer sp : nearby) {
            RpgStats.setBossDefeated(sp, true);
            ModMessages.syncPlayerToSelf(sp, false);
        }

        level.playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.HOSTILE, 1.5F, 1.0F);
        if (level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§6§l[¡VICTORIA TOTAL!] §e¡EvilMario cayó! La Base de Datos fue restaurada, los prisioneros son libres y has obtenido las reliquias ROOT: §cEspada DROP TABLE; §ey §cExotraje ADMIN-SQL§e!"), false);
        }
    }

    private void shoutRandomPhrase() {
        String fraseElegida = FRASES[this.random.nextInt(FRASES.length)];
        Component mensaje = Component.literal("§4[EvilMario] §f" + fraseElegida);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(25.0D));
        for (Player p : nearbyPlayers) {
            p.sendSystemMessage(mensaje);
        }
    }
}