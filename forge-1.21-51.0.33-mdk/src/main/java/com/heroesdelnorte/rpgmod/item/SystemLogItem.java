package com.heroesdelnorte.rpgmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SystemLogItem extends Item {

    public SystemLogItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Arreglo con pedazos de la historia (Lore)
            String[] loreMessages = {
                    "§8[Registro 01]§7: Intentamos compilar el proyecto, pero un error de sintaxis en la línea 404 empezó a borrar nuestros archivos...",
                    "§8[Registro 15]§7: La entidad se hace llamar 'EvilMario'. Ha tomado el control del servidor. No podemos desconectarlo.",
                    "§8[Registro 42]§7: Uriel, Josepe, si encuentran esto... la única forma de detenerlo es reuniendo los fragmentos en la Torre Central.",
                    "§8[Registro 99]§7: El cielo se oscurece. Los aldeanos están mutando en secuaces. La corrupción es casi del 100%..."
            };

            // Elegir un mensaje al azar
            String message = loreMessages[player.getRandom().nextInt(loreMessages.length)];

            // Mostrar el mensaje en el chat del jugador
            player.sendSystemMessage(Component.literal(message));

            // Reproducir un sonido de "computadora vieja" o "experiencia"
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Consumir el ítem (gastar 1)
            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}