package com.heroesdelnorte.rpgmod.registry;

import com.heroesdelnorte.rpgmod.entity.EvilMario;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    // 1. Creamos el catálogo (Registro) para las entidades de nuestro mod
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "rpgmod");

    // 2. Anotamos a EvilMario en el catálogo
    public static final RegistryObject<EntityType<EvilMario>> EVIL_MARIO =
            ENTITIES.register("evil_mario",
                    () -> EntityType.Builder.of(EvilMario::new, MobCategory.MONSTER)
                            .sized(1.0f, 2.0f) // Tamaño de la "hitbox" (caja de colisión): 1 bloque de ancho por 2 de alto
                            .build("evil_mario"));

    public static final RegistryObject<EntityType<NpcBase>> NPC_BASE =
            ENTITIES.register("npc_base",
                    () -> EntityType.Builder.of(NpcBase::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .build("npc_base"));

    public static void register(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        // NOTA: Si tu DeferredRegister de entidades se llama diferente a ENTITIES
        // (por ejemplo, ENTITY_TYPES), cambia el nombre aquí abajo:
        ENTITIES.register(eventBus);
    }
}