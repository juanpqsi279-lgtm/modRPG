package com.heroesdelnorte.rpgmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RpgWeapon extends Item {

    public RpgWeapon(Properties properties) {
        // Esto es suficiente en 1.21 para evitar apilamientos
        super(properties.stacksTo(1));
    }

    // Protege contra desgaste o destrucción al minar bloques (evita que se rompa y sea reemplazada)
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        return false;
    }

    // Protege contra desgaste al golpear entidades
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }
}