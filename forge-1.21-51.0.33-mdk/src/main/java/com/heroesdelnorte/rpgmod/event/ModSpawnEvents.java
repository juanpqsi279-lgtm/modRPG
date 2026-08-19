package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgmod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSpawnEvents {

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // Le decimos al juego que los EvilMinions siguen las mismas reglas de oscuridad que los monstruos normales
        event.register(ModEntities.EVIL_MINION.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}