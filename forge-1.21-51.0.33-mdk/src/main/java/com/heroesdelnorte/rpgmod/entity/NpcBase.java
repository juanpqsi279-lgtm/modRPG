package com.heroesdelnorte.rpgmod.entity;

import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

public class NpcBase extends PathfinderMob {

    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    // Sincronizadores de Misión en el propio NPC
    private static final EntityDataAccessor<Integer> DATA_QUEST_STAGE = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_QUEST_ACTIVE = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BOOLEAN);

    public NpcBase(EntityType<? extends PathfinderMob> entityType, Level level) { super(entityType, level); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN_ID, this.random.nextInt(15));
        builder.define(DATA_QUEST_STAGE, 0);
        builder.define(DATA_QUEST_ACTIVE, false);
    }

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, id); }
    public int getQuestStage() { return this.entityData.get(DATA_QUEST_STAGE); }
    public void setQuestStage(int stage) { this.entityData.set(DATA_QUEST_STAGE, stage); }
    public boolean isQuestActive() { return this.entityData.get(DATA_QUEST_ACTIVE); }
    public void setQuestActive(boolean active) { this.entityData.set(DATA_QUEST_ACTIVE, active); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
        tag.putInt("QuestStage", this.getQuestStage());
        tag.putBoolean("QuestActive", this.isQuestActive());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId")) this.setSkinId(tag.getInt("SkinId"));
        if (tag.contains("QuestStage")) this.setQuestStage(tag.getInt("QuestStage"));
        if (tag.contains("QuestActive")) this.setQuestActive(tag.getBoolean("QuestActive"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 50.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (this.level().isClientSide()) {
                openScreen(); // Abre la GUI en el cliente
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    // Método que dibuja la pantalla
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    private void openScreen() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new com.heroesdelnorte.rpgmod.client.gui.NpcScreen(this));
    }

    // Procesar los clics de los botones de la Interfaz
    public void handleMenuAction(ServerPlayer player, int action) {
        int stage = this.getQuestStage();
        boolean active = this.isQuestActive();

        switch (action) {
            case 1:
                player.sendSystemMessage(Component.literal("§e[Aldeano]§f: No lo sé, ni siquiera te conozco, comercia un poco conmigo y te puedo ayudar..."));
                break;
            case 2:
                String[] greetings = {"¡Hola, forastero!", "¿Qué te trae por este mundo corrompido?", "Prepárate bien, las noches son oscuras."};
                player.sendSystemMessage(Component.literal("§e[Aldeano]§f: " + greetings[player.getRandom().nextInt(greetings.length)]));
                break;
            case 3:
                if (stage == 5) {
                    player.sendSystemMessage(Component.literal("§e[Aldeano]§f: Ya te di las coordenadas. ¡Ve a buscar el cofre!"));
                } else if (active) {
                    checkQuestCompletion(player, stage);
                }
                break;
            case 4: // Aceptó la misión
                this.setQuestActive(true);
                showTitle(player, "¡Misión Activa!", "gold");
                showSubtitle(player, "Consigue " + getRequirementString(stage));
                player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                break;
            case 5: // Rechazó
                player.sendSystemMessage(Component.literal("§e[Aldeano]§f: Entiendo. Vuelve cuando tengas valor."));
                break;
        }
    }

    private void checkQuestCompletion(ServerPlayer player, int stage) {
        // Ahora todas las fases piden exclusivamente el Bit Corrupto
        Item item = ModItems.CORRUPTED_BIT.get();

        // Las cantidades exactas que definiste para cada misión
        int amount = switch (stage) {
            case 0 -> 8;
            case 1 -> 15;
            case 2 -> 20;
            case 3 -> 25;
            default -> 0;
        };

        if (hasItems(player, item, amount)) {
            consumeItems(player, item, amount);
            this.setQuestActive(false);

            // Recompensa base: 1 Fragmento de Jefe (Aplica para las 4 misiones)
            if (stage <= 3) {
                player.getInventory().add(new ItemStack(ModItems.BOSS_FRAGMENT.get(), 1));
            }

            // Recompensas adicionales según la etapa
            if (stage == 1) { // Misión 2: Armadura de diamante
                player.getInventory().add(new ItemStack(Items.DIAMOND_HELMET));
                player.getInventory().add(new ItemStack(Items.DIAMOND_CHESTPLATE));
                player.getInventory().add(new ItemStack(Items.DIAMOND_LEGGINGS));
                player.getInventory().add(new ItemStack(Items.DIAMOND_BOOTS));
            } else if (stage == 2) { // Misión 3: Espada Glitch
                player.getInventory().add(new ItemStack(ModItems.GLITCH_SWORD.get(), 1));
            } else if (stage == 3) { // Misión 4: Generación del cofre y el Mapa
                ServerLevel serverLevel = player.serverLevel();
                int targetX = player.getBlockX() + (player.getRandom().nextInt(21) + 20) * (player.getRandom().nextBoolean() ? 1 : -1);
                int targetZ = player.getBlockZ() + (player.getRandom().nextInt(21) + 20) * (player.getRandom().nextBoolean() ? 1 : -1);
                int targetY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, targetX, targetZ) - 3;
                BlockPos chestPos = new BlockPos(targetX, targetY, targetZ);

                ItemStack mapPaper = new ItemStack(Items.PAPER, 1);
                mapPaper.set(DataComponents.CUSTOM_NAME, Component.literal("§6Coordenadas: X=" + targetX + ", Y=" + targetY + ", Z=" + targetZ));

                serverLevel.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
                if (serverLevel.getBlockEntity(chestPos) instanceof ChestBlockEntity chestEntity) {
                    chestEntity.setItem(13, new ItemStack(ModItems.STRUCTURE_MAP.get()));
                }
                player.getInventory().add(mapPaper);

                // Marca invisible en los datos del jugador de que ya acabó la historia
                player.getPersistentData().putBoolean("RpgStoryCompleted", true);
            }

            this.setQuestStage(stage + 1); // Avanza a la siguiente fase

            showTitle(player, "¡Misión Completada!", "green");
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            player.sendSystemMessage(Component.literal("§e[Aldeano]§f: Aún no tienes " + getRequirementString(stage) + ". ¡Destruye a más corruptos!"));
        }
    }

    private void showTitle(ServerPlayer player, String text, String color) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput(), "title " + player.getScoreboardName() + " title {\"text\":\"" + text + "\",\"color\":\"" + color + "\"}");
    }
    private void showSubtitle(ServerPlayer player, String text) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput(), "title " + player.getScoreboardName() + " subtitle {\"text\":\"" + text + "\"}");
    }

    private boolean hasItems(Player player, Item item, int amount) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count >= amount;
    }

    private void consumeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (stack.getCount() >= remaining) { stack.shrink(remaining); break; }
                else { remaining -= stack.getCount(); stack.setCount(0); }
            }
        }
    }

    private String getRequirementString(int stage) {
        return switch (stage) {
            case 0 -> "8 Bits Corruptos";
            case 1 -> "15 Bits Corruptos";
            case 2 -> "20 Bits Corruptos";
            case 3 -> "25 Bits Corruptos";
            default -> "Nada";
        };
    }
}