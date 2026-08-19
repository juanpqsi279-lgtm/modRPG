package com.heroesdelnorte.rpgmod.event;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.entity.NpcBase;
import com.heroesdelnorte.rpgmod.item.RpgWeapon;
import com.heroesdelnorte.rpgmod.registry.ModEntities;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "rpgmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatHandler {

    private static final Map<UUID, Integer> hitCounters = new HashMap<>();

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        // Aplica a cualquier mob atacado por el jugador
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (target == player) return; // No auto-dañarse

            ItemStack heldItem = player.getMainHandItem();
            UUID playerId = player.getUUID();

            // +5 de daño por cada punto de habilidad comprado en el menú
            int bonusDmg = RpgStats.getBonusDamage(playerId) * 5;
            int currentHits = hitCounters.getOrDefault(playerId, 0) + 1;

            // 1. LLAVE INGLESA (URIEL): 12 base (+ daño extra). Al 3er golpe: 25 de daño
            if (heldItem.is(ModItems.WRENCH.get())) {
                if (currentHits >= 3) {
                    event.setAmount(25.0f + bonusDmg);
                    player.sendSystemMessage(Component.literal("§6[Uriel] ¡SUPER GOLPE CARGADO! (" + (25 + bonusDmg) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    event.setAmount(12.0f + bonusDmg);
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§6[Uriel] §7Golpe " + currentHits + "/3 (" + (12 + bonusDmg) + " de Daño)"));
                }
            }
            // 2. LÁTIGO (CHATGPT): 14 base (+ daño extra). Al 3er golpe: Velocidad
            else if (heldItem.is(ModItems.WHIP.get())) {
                event.setAmount(14.0f + bonusDmg);
                if (currentHits >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 1));
                    player.sendSystemMessage(Component.literal("§b[ChatGPT] ¡Velocidad activada por 30s! (" + (14 + bonusDmg) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§b[ChatGPT] §7Golpe " + currentHits + "/3 (" + (14 + bonusDmg) + " de Daño)"));
                }
            }
            // 3. HACHA (JOSEPE): 15 base (+ daño extra). Al 3er golpe: Invisibilidad y Fuerza
            else if (heldItem.is(ModItems.JOSEPE_AXE.get())) {
                event.setAmount(15.0f + bonusDmg);
                if (currentHits >= 3) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 25, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20 * 25, 1)); // Fuerza II
                    player.sendSystemMessage(Component.literal("§c[Josepe] ¡Fuerza e Invisibilidad por 25s! (" + (15 + bonusDmg) + " de Daño)"));
                    hitCounters.put(playerId, 0);
                } else {
                    hitCounters.put(playerId, currentHits);
                    player.sendSystemMessage(Component.literal("§c[Josepe] §7Golpe " + currentHits + "/3 (" + (15 + bonusDmg) + " de Daño)"));
                }
            }
        }
    }

    // 1. CANCELACIÓN ESTRICTA DE SOLTAR ÍTEMS (TECLA Q)
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (stack.is(ModItems.WRENCH.get()) || stack.is(ModItems.WHIP.get()) || stack.is(ModItems.JOSEPE_AXE.get())) {
            // Cancela la entidad arrojada
            event.setCanceled(true);

            Player player = event.getPlayer();
            // Reinserta el arma en el inventario si no la tiene
            if (!player.getInventory().contains(stack)) {
                player.getInventory().add(stack);
            }

            // Fuerza a sincronizar el inventario entre cliente y servidor inmediatamente
            player.containerMenu.broadcastChanges();
            player.sendSystemMessage(Component.literal("§c[!] Tu arma RPG es fija y no se puede tirar."));
        }
    }

    // REEMPLAZA ALDEANOS VANILLA POR TUS NPCS
    @SubscribeEvent
    public static void onEntityJoinLevel(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof net.minecraft.world.entity.npc.Villager villager) {
            if (!villager.isRemoved()) {
                NpcBase customNpc = ModEntities.NPC_BASE.get().create(event.getLevel());
                if (customNpc != null) {
                    customNpc.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                    event.getLevel().addFreshEntity(customNpc);
                    villager.discard(); // Elimina el aldeano vanilla
                }
            }
        }
    }

    // Evita que caigan al morir
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            event.getDrops().removeIf(drop -> {
                ItemStack stack = drop.getItem();
                return stack.is(ModItems.WRENCH.get()) ||
                        stack.is(ModItems.WHIP.get()) ||
                        stack.is(ModItems.JOSEPE_AXE.get());
            });
        }
    }

    // EVITA QUE CUALQUIER ÍTEM RECOGIDO REEMPLACE EL ARMA ACTIVA
    @SubscribeEvent
    public static void onItemPickup(net.minecraftforge.event.entity.player.EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        // Si tienes tu arma RPG en la mano activa, el juego no puede alterar esa casilla
        if (heldItem.getItem() instanceof RpgWeapon) {
            player.containerMenu.broadcastChanges();
        }
    }
}