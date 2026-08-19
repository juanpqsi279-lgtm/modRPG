package com.heroesdelnorte.rpgmod.block;

import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PortalBlock extends Block {

    // 1. Propiedad para saber si el USB está insertado
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public PortalBlock(Properties properties) {
        super(properties);
        // 2. Por defecto, el bloque nace desactivado (sin USB)
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    // 3. Registrar el estado en el bloque
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        // Si el bloque NO está activo y el jugador le da clic con el USB (BOSS_KEY)
        if (!state.getValue(ACTIVE) && stack.is(ModItems.BOSS_KEY.get())) {
            if (!level.isClientSide()) {
                // Quitar la llave de la mano
                stack.shrink(1);

                // ¡Cambiar el estado del bloque a ACTIVO (esto cambia la textura visual)!
                level.setBlock(pos, state.setValue(ACTIVE, true), 3);

                // Mensaje inicial
                level.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("§c[!] La base de datos se ha corrompido... Algo se acerca."), false);

                // Rayo visual inicial
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
                    lightning.setVisualOnly(true);
                    level.addFreshEntity(lightning);
                }

                // Iniciar cuenta regresiva de 5 segundos
                level.scheduleTick(pos, this, 100);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Invocar a Evil Mario
        EvilMario boss = ModEntities.EVIL_MARIO.get().create(level);
        if (boss != null) {
            boss.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
            level.addFreshEntity(boss);

            // Rayo de aparición
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(Vec3.atBottomCenterOf(pos.above()));
                lightning.setVisualOnly(true);
                level.addFreshEntity(lightning);
            }

            // Anuncio
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§4[!!!] ¡EvilMario ha llegado... Prepárate para morir!"), false);

            // Destruir el portal
            level.destroyBlock(pos, false);
        }
    }
}