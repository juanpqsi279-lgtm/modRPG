package com.heroesdelnorte.rpgmod.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class RpgArmorItem extends ArmorItem {

    private final String tierId;

    public RpgArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, String tierId) {
        super(material, type, properties);
        this.tierId = tierId;
    }

    public String getTierId() {
        return tierId;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if ("admin_sql".equals(tierId)) {
            tooltipComponents.add(Component.literal("§c§l✦ Exotraje ROOT: ADMIN-SQL MK.I"));
            tooltipComponents.add(Component.literal("§6Reliquia exclusiva de EvilMario (No crafteable)"));
            tooltipComponents.add(Component.literal("§7Set completo (4 piezas):"));
            tooltipComponents.add(Component.literal("§b  • Propulsores de Vuelo (Doble Espacio en Supervivencia)"));
            tooltipComponents.add(Component.literal("§b  • Rayo Repulsor SQL: Tecla [V] o Shift+Clic Derecho (mano vacía)"));
            tooltipComponents.add(Component.literal("§b  • Resistencia III, Regeneración II y Visor Nocturno"));
            tooltipComponents.add(Component.literal("§b  • Inmunidad a Caídas, Rayos y Fuego | -35% daño de jefes"));
        } else if ("quantum".equals(tierId)) {
            tooltipComponents.add(Component.literal("§d✦ Armadura Definitiva Anti-EvilMario"));
            tooltipComponents.add(Component.literal("§7Set completo (4 piezas):"));
            tooltipComponents.add(Component.literal("§b  • Resistencia II y Regeneración en combate"));
            tooltipComponents.add(Component.literal("§b  • Inmunidad a Rayos y Fuego"));
            tooltipComponents.add(Component.literal("§b  • -25% de daño recibido de EvilMario y Secuaces"));
        } else if ("firewall".equals(tierId)) {
            tooltipComponents.add(Component.literal("§9✦ Blindaje Digital Cortafuegos"));
            tooltipComponents.add(Component.literal("§7Set completo (4 piezas):"));
            tooltipComponents.add(Component.literal("§3  • Resistencia I y Velocidad I constantes"));
        }
    }

    public static boolean hasFullAdminSqlSet(Player player) {
        return hasFullSet(player, "admin_sql");
    }

    public static boolean hasFullFirewallSet(Player player) {
        return hasFullSet(player, "firewall");
    }

    public static boolean hasFullQuantumSet(Player player) {
        return hasFullSet(player, "quantum");
    }

    private static boolean hasFullSet(Player player, String targetTier) {
        if (player == null) {
            return false;
        }
        return matchesTier(player.getItemBySlot(EquipmentSlot.HEAD), targetTier)
                && matchesTier(player.getItemBySlot(EquipmentSlot.CHEST), targetTier)
                && matchesTier(player.getItemBySlot(EquipmentSlot.LEGS), targetTier)
                && matchesTier(player.getItemBySlot(EquipmentSlot.FEET), targetTier);
    }

    private static boolean matchesTier(ItemStack stack, String targetTier) {
        return stack.getItem() instanceof RpgArmorItem armor && targetTier.equals(armor.getTierId());
    }
}
