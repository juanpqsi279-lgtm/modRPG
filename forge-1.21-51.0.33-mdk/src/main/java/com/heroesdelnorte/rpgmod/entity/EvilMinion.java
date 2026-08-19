package com.heroesdelnorte.rpgmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

public class EvilMinion extends Monster {

    public EvilMinion(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Comportamiento de IA: Atacar jugadores y patrullar
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Objetivo principal: Buscar y atacar jugadores
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D) // 15 corazones (un zombi normal tiene 10)
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // ¡Son mucho más rápidos!
                .add(Attributes.ATTACK_DAMAGE, 5.0D) // Quitan 2.5 corazones por golpe sin armadura
                .add(Attributes.FOLLOW_RANGE, 35.0D); // Te detectan desde más lejos
    }
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        // 75% de probabilidad de que suelte la moneda si fue golpeado por el jugador
        if (recentlyHit && this.random.nextFloat() < 0.75F) {
            // Soltará entre 1 y 3 Bits Corruptos al morir
            int amountToDrop = 1 + this.random.nextInt(3);
            this.spawnAtLocation(com.heroesdelnorte.rpgmod.registry.ModItems.CORRUPTED_BIT.get(), amountToDrop);
        }
    }

}