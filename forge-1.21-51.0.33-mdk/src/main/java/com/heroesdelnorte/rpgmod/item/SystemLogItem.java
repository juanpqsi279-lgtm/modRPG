package com.heroesdelnorte.rpgmod.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SystemLogItem extends Item {

    private static final String TAG_LOG_INDEX = "RpgSystemLogIndex";

    private static final String[] LORE_ENTRIES = {
            "§8[Registro 01 - Error Línea 404]§7: Intentamos compilar el núcleo, pero una excepción en la línea 404 despertó a §4EvilMario§7. Secuestró el servidor en cuestión de minutos.",
            "§8[Registro 02 - Torre de Cristal]§7: §bEvilMario §7alzó una Torre de Base de Datos de 5 pisos con paredes de cristal y anillos de seguridad. Sus §cSecuaces Corruptos §7patrullan cada nivel.",
            "§8[Registro 03 - Los Prisioneros]§7: Capturó a nuestros ingenieros (Carlos, Valeria y Tomás) y los encerró en jaulas en los Pisos 2 y 4. Si sigues vivo, busca cómo hablar con ellos.",
            "§8[Registro 04 - Compilación de la Key]§7: Los §d4 Fragmentos de Base de Datos §7están encriptados: ninguna mesa de crafteo puede unirlos. Solo los §bPrisioneros §7memorizaron cómo compilarlos con un §cBloque de Redstone§7.",
            "§8[Registro 05 - El Domo Blanco]§7: En la cima blanca aguarda el Núcleo de Invocación. Al insertar la Key caerá un rayo maestro y bajará §4EvilMario§7. Trae Armadura Cuántica y Tótems.",
            "§8[Registro 06 - Reliquias ROOT]§7: Archivo clasificado: EvilMario custodia la §4Espada DROP TABLE; §7y el §cExotraje ADMIN-SQL §7(vuelo y rayos repulsores). Solo caen al derrotarlo."
    };

    public SystemLogItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            CompoundTag data = player.getPersistentData();
            int index = Math.floorMod(data.getInt(TAG_LOG_INDEX), LORE_ENTRIES.length);
            data.putInt(TAG_LOG_INDEX, (index + 1) % LORE_ENTRIES.length);

            player.sendSystemMessage(Component.literal(LORE_ENTRIES[index]));
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F,
                    1.1F);

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("§7Archivo recuperado del servidor (Clic derecho para descifrar)"));
    }
}
