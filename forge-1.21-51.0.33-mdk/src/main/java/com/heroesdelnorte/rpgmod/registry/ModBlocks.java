package com.heroesdelnorte.rpgmod.registry;

import com.heroesdelnorte.rpgmod.block.PortalBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "rpgmod");

    // Registro del bloque del portal
    public static final RegistryObject<Block> PORTAL_BLOCK = BLOCKS.register("portal_block",
            () -> new PortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(5.0f).requiresCorrectToolForDrops()));

    // Registro del bloque como ítem para el inventario
    public static final RegistryObject<Item> PORTAL_BLOCK_ITEM = ModItems.ITEMS.register("portal_block",
            () -> new BlockItem(PORTAL_BLOCK.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}