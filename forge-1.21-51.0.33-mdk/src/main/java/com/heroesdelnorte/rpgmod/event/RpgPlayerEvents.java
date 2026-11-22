package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.SyncPlayerDataPayload;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "rpgmod")
public class RpgPlayerEvents {

    private static final String FIRST_JOIN_TAG = "rpgmod_has_spawned";

    // 1. Teletransporte seguro a la aldea en la primera conexión y sincronización al iniciar sesión
    @SubscribeEvent
    public static void onPlayerFirstJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.getBoolean(FIRST_JOIN_TAG)) {
            persistentData.putBoolean(FIRST_JOIN_TAG, true);
            ServerLevel level = player.serverLevel();

            // Busca la aldea más cercana en el radio inicial
            BlockPos villagePos = level.findNearestMapStructure(StructureTags.VILLAGE, player.blockPosition(), 100, false);

            if (villagePos != null) {
                // Cargar el chunk antes de consultar el Heightmap para no recibir la altura mínima del mundo
                level.getChunk(villagePos.getX() >> 4, villagePos.getZ() >> 4);
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, villagePos.getX(), villagePos.getZ());

                if (surfaceY > level.getMinBuildHeight()) {
                    BlockPos targetPos = new BlockPos(villagePos.getX(), surfaceY, villagePos.getZ());
                    while (targetPos.getY() < level.getMaxBuildHeight() - 2
                            && (level.getBlockState(targetPos).blocksMotion()
                            || level.getBlockState(targetPos.above()).blocksMotion())) {
                        targetPos = targetPos.above();
                    }

                    player.teleportTo(level, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
                    player.setRespawnPosition(level.dimension(), targetPos, player.getYRot(), true, false);
                    player.sendSystemMessage(Component.literal("§b[Incidente 404] §aHas despertado en una aldea de sobrevivientes. Habla con los §eAldeanos (Clic derecho) §apara descubrir la historia de EvilMario y la Torre de Base de Datos."));
                }
            }
        }

        String activeClass = RpgStats.getActiveClass(player);
        if (!activeClass.isEmpty()) {
            RpgStats.applyAllPlayerAttributes(player);
            CharacterInventoryManager.ensureWeapon(player, activeClass);
        }
        ModMessages.syncPlayerToTrackingAndSelf(player, activeClass.isEmpty());
    }

    // 2. Reemplazo seguro de aldeanos vanilla por NpcBase (sin modificar la lista de entidades durante el evento)
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof Villager villager
                && event.getLevel() instanceof ServerLevel serverLevel) {
            double x = villager.getX();
            double y = villager.getY();
            double z = villager.getZ();
            float yRot = villager.getYRot();
            float xRot = villager.getXRot();

            event.setCanceled(true);

            serverLevel.getServer().execute(() -> {
                BlockPos spawnPos = BlockPos.containing(x, y, z);
                if (serverLevel.isLoaded(spawnPos)) {
                    NpcBase customNpc = ModEntities.NPC_BASE.get().create(serverLevel);
                    if (customNpc != null) {
                        customNpc.moveTo(x, y, z, yRot, xRot);
                        serverLevel.addFreshEntity(customNpc);
                    }
                }
            });
        }
    }

    // 3. Evitar que las armas de clase caigan al suelo al morir
    @SubscribeEvent
    public static void onPlayerDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            event.getDrops().removeIf(drop -> drop.getItem().getItem() instanceof RpgWeapon);
        }
    }

    // 4. Al morir o regresar del End, transferir los datos persistentes al nuevo cuerpo
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        newPlayer.getPersistentData().merge(oldPlayer.getPersistentData());
    }

    // 5. Al reaparecer, reaplicar atributos, vida máxima, arma y sincronizar al cliente
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String activeClass = RpgStats.getActiveClass(player);
            RpgStats.applyAllPlayerAttributes(player);
            if (!event.isEndConquered()) {
                player.setHealth(player.getMaxHealth());
            }
            if (!activeClass.isEmpty()) {
                CharacterInventoryManager.ensureWeapon(player, activeClass);
            }
            ModMessages.syncPlayerToTrackingAndSelf(player, activeClass.isEmpty());
        }
    }

    // 6. Al cambiar de dimensión, reaplicar atributos y sincronizar skins
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RpgStats.applyAllPlayerAttributes(player);
            ModMessages.syncPlayerToTrackingAndSelf(player, false);
        }
    }

    // 7. Cuando otro jugador empieza a observar a este jugador, sincronizarle su skin de clase
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer tracker) {
            ModMessages.sendToPlayer(
                    new SyncPlayerDataPayload(target.getId(), RpgStats.getActiveClass(target), 0, 0, false, 0, false, false, 0, 0, 0, false, false),
                    tracker
            );
        }
    }

    // 8. Limpiar cachés en memoria al desconectarse
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        CombatHandler.clearPlayer(playerId);
        RpgStats.clearPlayer(playerId);
    }
}