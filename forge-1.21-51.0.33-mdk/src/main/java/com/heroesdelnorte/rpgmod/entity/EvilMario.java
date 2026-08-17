package com.heroesdelnorte.rpgmod.entity;

import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.Level;

import java.util.List;

public class EvilMario extends Monster {

    private int dialogueCooldown = 0;

    private static final String[] FRASES = {
            "Maldita IA",
            "Preguntaselo al chat",
            "Yo vengo a jalar",
            "Ando a raja tabla"
    };

    public EvilMario(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
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

    private void shoutRandomPhrase() {
        String fraseElegida = FRASES[this.random.nextInt(FRASES.length)];
        Component mensaje = Component.literal("§4[EvilMario] §f" + fraseElegida);
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(25.0D));
        for (Player p : nearbyPlayers) {
            p.sendSystemMessage(mensaje);
        }
    }
}