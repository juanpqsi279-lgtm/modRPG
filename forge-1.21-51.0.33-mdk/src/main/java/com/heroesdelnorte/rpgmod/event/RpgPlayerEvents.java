package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgmod")
public class RpgPlayerEvents {

    private static final String FIRST_JOIN_TAG = "rpgmod_has_spawned";

    // 1. Teletransporte a la aldea en la primera conexión
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
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, villagePos.getX(), villagePos.getZ());
                BlockPos targetPos = new BlockPos(villagePos.getX(), surfaceY + 1, villagePos.getZ());

                // Mueve al jugador y ancla su punto de reaparición ahí
                player.teleportTo(level, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
                player.setRespawnPosition(level.dimension(), targetPos, player.getYRot(), true, false);

                player.sendSystemMessage(Component.literal("§a[RPG] Has despertado en una aldea segura. Habla con los Aldeanos Sospechosos para comenzar."));
            }
        }
    }

    // 2. Reemplazo automático: Ningún aldeano vainilla puede existir, se convierten en NpcBase
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Villager villager) {
            if (!villager.isRemoved()) {
                NpcBase customNpc = ModEntities.NPC_BASE.get().create(event.getLevel());
                if (customNpc != null) {
                    customNpc.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                    event.getLevel().addFreshEntity(customNpc);
                    villager.discard(); // Borra al aldeano vainilla de la memoria
                }
            }
        }
    }

    // 3. Evitar que las armas de clase caigan al suelo al morir
    @SubscribeEvent
    public static void onPlayerDeath(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            event.getDrops().removeIf(drop -> {
                Item item = drop.getItem().getItem();
                return item == ModItems.WRENCH.get() ||
                        item == ModItems.WHIP.get() ||
                        item == ModItems.JOSEPE_AXE.get();
            });
        }
    }

    // 4. Al revivir, transferir los datos del cadáver al nuevo cuerpo
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            newPlayer.getPersistentData().merge(oldPlayer.getPersistentData());
        }
    }

    // 5. Al aparecer en la cama, leer qué clase era y devolverle su arma
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        String activeClass = player.getPersistentData().getString("ActiveRPGClass");
        if (activeClass != null && !activeClass.isEmpty()) {
            CharacterInventoryManager.ensureWeapon(player, activeClass);
        }
    }
}