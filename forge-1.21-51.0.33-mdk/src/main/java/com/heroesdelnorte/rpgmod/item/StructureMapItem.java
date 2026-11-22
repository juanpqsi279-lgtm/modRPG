package com.heroesdelnorte.rpgmod.item;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import com.heroesdelnorte.rpgmod.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class StructureMapItem extends Item {

    private static final String TAG_HAS_COORDS = "HasCoords";
    private static final String TAG_X = "PyramidX";
    private static final String TAG_Y = "PyramidY";
    private static final String TAG_Z = "PyramidZ";

    public StructureMapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createBoundMap(BlockPos pyramidPos) {
        ItemStack stack = new ItemStack(ModItems.STRUCTURE_MAP.get(), 1);
        writeCoords(stack, pyramidPos);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§6Mapa de la Torre de Base de Datos"));
        return stack;
    }

    public static void writeCoords(ItemStack stack, BlockPos pos) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putBoolean(TAG_HAS_COORDS, true);
            tag.putInt(TAG_X, pos.getX());
            tag.putInt(TAG_Y, pos.getY());
            tag.putInt(TAG_Z, pos.getZ());
        });
    }

    public static BlockPos readCoords(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.getBoolean(TAG_HAS_COORDS)) {
            return null;
        }
        return new BlockPos(tag.getInt(TAG_X), tag.getInt(TAG_Y), tag.getInt(TAG_Z));
    }

    public static BlockPos findOrLocatePyramid(ServerLevel level, BlockPos origin) {
        BlockPos found = level.findNearestMapStructure(ModTags.EVIL_PYRAMID, origin, 100, false);
        if (found == null) {
            return null;
        }
        level.getChunk(found.getX() >> 4, found.getZ() >> 4);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, found.getX(), found.getZ());
        // PortalBlock está en la cima blanca de la Torre de Base de Datos (piso 5, y=29)
        int altarY = Math.max(level.getMinBuildHeight() + 29, surfaceY + 29);
        return new BlockPos(found.getX(), altarY, found.getZ());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BlockPos target = readCoords(stack);
            if (target == null && RpgStats.hasPyramidCoords(player)) {
                target = RpgStats.getPyramidPos(player);
                if (target != null) {
                    writeCoords(stack, target);
                }
            }
            if (target == null) {
                target = findOrLocatePyramid(serverLevel, player.blockPosition());
                if (target != null) {
                    writeCoords(stack, target);
                    RpgStats.setPyramidPos(player, target);
                }
            }

            if (target == null) {
                player.sendSystemMessage(Component.literal("§c[Mapa] No se encontró ninguna Torre de Base de Datos cercana en esta dimensión."));
                return InteractionResultHolder.fail(stack);
            }

            double dx = target.getX() + 0.5D - player.getX();
            double dz = target.getZ() + 0.5D - player.getZ();
            double horizDist = Math.hypot(dx, dz);
            int distBlocks = (int) Math.round(horizDist);
            String direction = getDirectionName(dx, dz);

            player.sendSystemMessage(Component.literal(
                    "§6[Mapa] §bTorre de Base de Datos de EvilMario en §fX=" + target.getX()
                            + ", Y=" + target.getY() + ", Z=" + target.getZ()
                            + " §7(" + distBlocks + " bloques hacia el " + direction + ")"
            ));

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.1F);
            player.getCooldowns().addCooldown(this, 30);

            if (horizDist > 1.0D) {
                double dirX = dx / horizDist;
                double dirZ = dz / horizDist;
                double baseY = player.getY() + 1.2D;
                for (int i = 1; i <= 12; i++) {
                    double px = player.getX() + dirX * i;
                    double pz = player.getZ() + dirZ * i;
                    double py = baseY + i * 0.12D;
                    serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 2, 0.05D, 0.05D, 0.05D, 0.01D);
                    serverLevel.sendParticles(ParticleTypes.PORTAL, px, py, pz, 3, 0.15D, 0.15D, 0.15D, 0.02D);
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        BlockPos coords = readCoords(stack);
        if (coords != null) {
            tooltipComponents.add(Component.literal("§bTorre Base de Datos: §fX=" + coords.getX() + ", Y=" + coords.getY() + ", Z=" + coords.getZ()));
            tooltipComponents.add(Component.literal("§7Clic derecho para mostrar dirección y partículas guía"));
        } else {
            tooltipComponents.add(Component.literal("§7Clic derecho para localizar la Torre de Base de Datos"));
        }
    }

    private static String getDirectionName(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) {
            angle += 360.0D;
        }
        int index = (int) Math.round(angle / 45.0D) % 8;
        return switch (index) {
            case 0 -> "Norte";
            case 1 -> "Noreste";
            case 2 -> "Este";
            case 3 -> "Sureste";
            case 4 -> "Sur";
            case 5 -> "Suroeste";
            case 6 -> "Oeste";
            default -> "Noroeste";
        };
    }
}
