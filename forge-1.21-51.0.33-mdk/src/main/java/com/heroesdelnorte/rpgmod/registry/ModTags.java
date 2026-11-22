package com.heroesdelnorte.rpgmod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ModTags {

    public static final TagKey<Structure> EVIL_PYRAMID = TagKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath("rpgmod", "evil_pyramid")
    );
}
