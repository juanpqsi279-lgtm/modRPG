package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rpgmod")
public class NpcCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rpg_npc")
                .then(Commands.argument("action", IntegerArgumentType.integer())
                        .then(Commands.argument("npcid", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int action = IntegerArgumentType.getInteger(context, "action");
                                    int npcId = IntegerArgumentType.getInteger(context, "npcid");

                                    Entity entity = player.serverLevel().getEntity(npcId);
                                    if (entity instanceof NpcBase npc) {
                                        npc.handleMenuAction(player, action);
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }
}