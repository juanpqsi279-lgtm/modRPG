package com.heroesdelnorte.rpgmod.entity;

import com.heroesdelnorte.rpgmod.RpgStats;
import com.heroesdelnorte.rpgmod.client.ClientHooks;
import com.heroesdelnorte.rpgmod.item.StructureMapItem;
import com.heroesdelnorte.rpgmod.network.ModMessages;
import com.heroesdelnorte.rpgmod.network.NpcDialoguePayload;
import com.heroesdelnorte.rpgmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;

public class NpcBase extends PathfinderMob {

    public static final int SKIN_COUNT = 14;

    private static final String[] NPC_NAMES = {
            "Sobreviviente Alex",
            "Ingeniero Mateo",
            "Hacker Sofía",
            "Explorador Diego",
            "Guardia Elena",
            "Cronista Luis",
            "Artesano Bruno",
            "Analista Carla"
    };

    private static final EntityDataAccessor<Integer> DATA_SKIN_ID = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_QUEST_STAGE = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_QUEST_ACTIVE = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> DATA_NPC_NAME = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_IS_PRISONER = SynchedEntityData.defineId(NpcBase.class, EntityDataSerializers.BOOLEAN);

    public NpcBase(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN_ID, this.random.nextInt(SKIN_COUNT));
        builder.define(DATA_QUEST_STAGE, 0);
        builder.define(DATA_QUEST_ACTIVE, false);
        builder.define(DATA_NPC_NAME, NPC_NAMES[this.random.nextInt(NPC_NAMES.length)]);
        builder.define(DATA_IS_PRISONER, false);
    }

    public int getSkinId() { return this.entityData.get(DATA_SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(DATA_SKIN_ID, Math.floorMod(id, SKIN_COUNT)); }
    public int getQuestStage() { return this.entityData.get(DATA_QUEST_STAGE); }
    public void setQuestStage(int stage) { this.entityData.set(DATA_QUEST_STAGE, stage); }
    public boolean isQuestActive() { return this.entityData.get(DATA_QUEST_ACTIVE); }
    public void setQuestActive(boolean active) { this.entityData.set(DATA_QUEST_ACTIVE, active); }
    public boolean isPrisoner() { return this.entityData.get(DATA_IS_PRISONER); }

    public void setPrisoner(boolean prisoner) {
        this.entityData.set(DATA_IS_PRISONER, prisoner);
        updatePrisonerNameplate();
    }

    public String getNpcName() {
        String name = this.entityData.get(DATA_NPC_NAME);
        return (name == null || name.isEmpty()) ? "Aldeano Sospechoso" : name;
    }

    public void setNpcName(String name) {
        if (name != null && !name.isEmpty()) {
            this.entityData.set(DATA_NPC_NAME, name);
            updatePrisonerNameplate();
        }
    }

    private void updatePrisonerNameplate() {
        if (this.isPrisoner()) {
            this.setCustomName(Component.literal("§b[Prisionero] §f" + getNpcName()));
            this.setCustomNameVisible(true);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", this.getSkinId());
        tag.putInt("QuestStage", this.getQuestStage());
        tag.putBoolean("QuestActive", this.isQuestActive());
        tag.putString("NpcName", this.getNpcName());
        tag.putBoolean("IsPrisoner", this.isPrisoner());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinId")) this.setSkinId(Math.floorMod(tag.getInt("SkinId"), SKIN_COUNT));
        if (tag.contains("QuestStage")) this.setQuestStage(tag.getInt("QuestStage"));
        if (tag.contains("QuestActive")) this.setQuestActive(tag.getBoolean("QuestActive"));
        if (tag.contains("NpcName")) this.setNpcName(tag.getString("NpcName"));
        if (tag.contains("IsPrisoner")) this.setPrisoner(tag.getBoolean("IsPrisoner"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6D) {
            @Override
            public boolean canUse() {
                return !NpcBase.this.isPrisoner() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !NpcBase.this.isPrisoner() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isPrisoner() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (this.isPrisoner() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 50.0D).add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            if (this.level().isClientSide()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openNpcScreen(this));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    public void handleMenuAction(ServerPlayer player, int action) {
        handleMenuAction(player, action, 0);
    }

    public void handleMenuAction(ServerPlayer player, int action, int param) {
        int stage = RpgStats.getQuestStage(player);
        boolean active = RpgStats.isQuestActive(player);
        String prefix = this.isPrisoner() ? "§b[" + getNpcName() + "]§f: " : "§e[" + getNpcName() + "]§f: ";

        // Curación gratuita compartida entre aldeanos y prisioneros
        if (action == 2 && param == 1) {
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(6.0F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            String msg = "§a¡Te he restaurado la salud y el hambre al 100%, héroe!";
            sendReply(player, msg, prefix + msg);
            return;
        }

        // Acciones exclusivas de PRISIONEROS en la Torre de Base de Datos
        if (this.isPrisoner()) {
            handlePrisonerAction(player, action, param, prefix);
            return;
        }

        switch (action) {
            case 1 -> handleVillageTalk(player, param, stage, prefix);
            case 2 -> {
                String[] greetings = {
                        "¡Hola, héroe! Desde el error de la Línea 404 resistimos en esta aldea.",
                        "¿Ya mejoraste tu +Salud y +Daño en el menú de la tecla [R]?",
                        "Cuidado de noche: los Secuaces Corruptos rondan buscando más ingenieros."
                };
                String chosen = greetings[player.getRandom().nextInt(greetings.length)];
                sendReply(player, chosen, prefix + chosen);
            }
            case 3 -> {
                if (stage >= 4) {
                    String msg = "Ya completaste los 4 capítulos. Lleva tus 4 Fragmentos + 1 Bloque de Redstone a los Prisioneros dentro de la Torre de Base de Datos.";
                    sendReply(player, msg, prefix + msg);
                } else if (active) {
                    checkQuestCompletion(player, stage, prefix);
                } else {
                    String msg = "Primero pulsa 'Aceptar Capítulo' en la pestaña de Historia.";
                    sendReply(player, msg, prefix + msg);
                }
            }
            case 4 -> {
                if (stage >= 4) {
                    String msg = "Ya completaste todos los capítulos de la historia.";
                    sendReply(player, msg, prefix + msg);
                    break;
                }
                RpgStats.setQuestActive(player, true);
                this.setQuestActive(true);
                showTitle(player, "¡Capítulo " + (stage + 1) + "/4 Activo!", ChatFormatting.GOLD);
                showSubtitle(player, "Consigue " + getRequirementString(stage));
                player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                ModMessages.syncPlayerToSelf(player, false);
                String msg = "§aCapítulo " + (stage + 1) + "/4 aceptado. Reúne §e" + getRequirementString(stage) + " §ay entrégalos aquí.";
                sendReply(player, msg, prefix + msg);
            }
            case 5 -> sendReply(player, "Entiendo. Vuelve cuando estés preparado.", prefix + "Entiendo. Vuelve cuando estés preparado.");
            case 6 -> handleShopPurchase(player, param, prefix);
            case 7 -> {
                if (stage >= 4) {
                    BlockPos towerPos = RpgStats.getPyramidPos(player);
                    if (towerPos == null) {
                        towerPos = StructureMapItem.findOrLocatePyramid(player.serverLevel(), player.blockPosition());
                        if (towerPos != null) {
                            RpgStats.setPyramidPos(player, towerPos);
                        }
                    }
                    if (towerPos != null) {
                        giveOrDrop(player, StructureMapItem.createBoundMap(towerPos));
                        String msg = "§aCopia del Mapa de la Torre entregada (§fX=" + towerPos.getX() + ", Y=" + towerPos.getY() + ", Z=" + towerPos.getZ() + "§a).";
                        sendReply(player, msg, prefix + msg);
                        ModMessages.syncPlayerToSelf(player, false);
                    } else {
                        String msg = "§cNo se detectó ninguna Torre de Base de Datos generada en esta dimensión.";
                        sendReply(player, msg, prefix + msg);
                    }
                }
            }
        }
    }

    private void handlePrisonerAction(ServerPlayer player, int action, int param, String prefix) {
        switch (action) {
            case 10 -> { // Diálogos de los prisioneros dentro de la torre
                String reply = switch (param) {
                    case 0 -> "¡Al fin alguien llega! EvilMario nos secuestró durante el Incidente 404 y construyó esta Torre de Cristal. Nos encerró aquí mientras sus Secuaces vigilan los servidores.";
                    case 1 -> "Llevamos meses observándolo compilar comandos desde estas jaulas. Memorizamos su fórmula secreta: si nos entregas §d4 Fragmentos de Base de Datos §fy §c1 Bloque de Redstone§f, podemos compilar el §6Código de Invocación Corrupto§f y soltártelo al instante.";
                    default -> "§eAdvertencia táctica: §fEl Núcleo está arriba, en el Domo Blanco (Piso 5). Al insertar la Key caerá un rayo y bajará EvilMario. Corre el rumor de que solo él guarda la §4Espada DROP TABLE; §fy el §cExotraje ADMIN-SQL §f(¡que te permite volar y disparar rayos!).";
                };
                sendReply(player, reply, prefix + reply);
            }
            case 11 -> { // Compilar y soltar la Boss Key
                int frags = countItems(player, ModItems.BOSS_FRAGMENT.get());
                int redstone = countItems(player, Items.REDSTONE_BLOCK);
                if (frags < 4 || redstone < 1) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    String err = "§cMateriales insuficientes para compilar la Key. Necesitas §d4 Fragmentos §c(tienes " + frags + "/4) y §c1 Bloque de Redstone §c(tienes " + redstone + "/1).";
                    sendReply(player, err, prefix + err);
                    return;
                }

                consumeItems(player, ModItems.BOSS_FRAGMENT.get(), 4);
                consumeItems(player, Items.REDSTONE_BLOCK, 1);
                RpgStats.setKeyForged(player, true);

                // Soltar físicamente la BOSS_KEY hacia el jugador
                ItemStack keyStack = new ItemStack(ModItems.BOSS_KEY.get(), 1);
                Vec3 dir = player.position().add(0, 1.0D, 0).subtract(this.position().add(0, 1.1D, 0));
                if (dir.lengthSqr() > 0.001D) {
                    dir = dir.normalize();
                } else {
                    dir = new Vec3(0.0D, 0.1D, 0.25D);
                }
                Vec3 spawnPos = this.position().add(dir.x * 0.9D, 1.15D, dir.z * 0.9D);
                ItemEntity droppedKey = new ItemEntity(player.level(), spawnPos.x, spawnPos.y, spawnPos.z, keyStack);
                droppedKey.setNoPickUpDelay();
                droppedKey.setDeltaMovement(dir.x * 0.28D, 0.18D, dir.z * 0.28D);
                player.level().addFreshEntity(droppedKey);

                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawnPos.x, spawnPos.y, spawnPos.z, 25, 0.35D, 0.35D, 0.35D, 0.08D);
                }
                player.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
                player.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.4F);

                showTitle(player, "¡Código Compilado!", ChatFormatting.GOLD);
                showSubtitle(player, "Recoge la Key y sube al Domo Blanco (Piso 5)");
                player.containerMenu.broadcastChanges();
                ModMessages.syncPlayerToSelf(player, false);

                String ok = "§a¡Compilación exitosa! Te lancé el §6Código de Invocación Corrupto§a. Sube a la cima blanca, insértalo en el Núcleo y acaba con EvilMario para liberarnos.";
                sendReply(player, ok, prefix + ok);
            }
            case 12 -> { // Canje de emergencia: 8 Bits Corruptos -> 1 Bloque de Redstone
                Item bit = ModItems.CORRUPTED_BIT.get();
                if (!hasItems(player, bit, 8)) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    String err = "§cNecesitas 8 Bits Corruptos para ensamblar 1 Bloque de Redstone de emergencia.";
                    sendReply(player, err, prefix + err);
                    return;
                }
                consumeItems(player, bit, 8);
                giveOrDrop(player, new ItemStack(Items.REDSTONE_BLOCK, 1));
                player.containerMenu.broadcastChanges();
                player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
                String ok = "§a¡Listo! Aquí tienes §c1 Bloque de Redstone §alistísimo para compilar la Key.";
                sendReply(player, ok, prefix + ok);
            }
            default -> {
                String hint = "§b¡Rápido! Necesito §d4 Fragmentos de Base de Datos §by §c1 Bloque de Redstone §bpara compilar y soltarte la Key de EvilMario.";
                sendReply(player, hint, prefix + hint);
            }
        }
    }

    private void handleVillageTalk(ServerPlayer player, int topic, int stage, String prefix) {
        String reply;
        switch (topic) {
            case 0 -> reply = "Todo comenzó con el §bError de la Línea 404§f: EvilMario tomó el servidor, levantó una §bTorre de Cristal de Base de Datos §fy secuestró a nuestros ingenieros en jaulas. Solo logramos rescatar 4 Fragmentos encriptados.";
            case 1 -> reply = "Los §cSecuaces Corruptos §fmerodean de noche y dentro de la Torre (ahora menos veloces para que puedas combatirlos). Al caer sueltan §dBits Corruptos§f: úsalos en los 4 Capítulos de Historia para obtener los 4 Fragmentos y equípate en la Tienda.";
            case 2 -> {
                if (RpgStats.hasPyramidCoords(player)) {
                    BlockPos pos = RpgStats.getPyramidPos(player);
                    int dist = (int) Math.round(Math.hypot(pos.getX() - player.getX(), pos.getZ() - player.getZ()));
                    reply = "La §bTorre de Base de Datos §festá en §6X=" + pos.getX() + ", Y=" + pos.getY() + ", Z=" + pos.getZ()
                            + " §f(a ~" + dist + " bloques). Usa tu Mapa (clic derecho) para ver la ruta con partículas.";
                } else {
                    reply = "Aún estamos triangulando la Torre. Completa los 4 Capítulos de Historia conmigo (Cap. actual: "
                            + (Math.min(stage, 3) + 1) + "/4) para descifrar sus coordenadas reales.";
                }
            }
            default -> {
                if (RpgStats.isBossDefeated(player)) {
                    reply = "§6¡Leyenda! §fDerrotaste a EvilMario y liberaste la Base de Datos. Prueba tu §4Espada DROP TABLE; §f(clic derecho) y tu §cExotraje ADMIN-SQL §f(doble salto para volar + tecla [V] para lanzar rayos repulsores).";
                } else {
                    reply = "§eSecretos de la Torre: §fNinguna mesa de crafteo puede armar la Key. Lleva los §d4 Fragmentos + 1 Bloque de Redstone §fa los §bPrisioneros en jaulas §f(Pisos 2 y 4) para que la compilen. ¡Ve con Armadura Cuántica porque EvilMario guarda reliquias ROOT!";
                }
            }
        }
        sendReply(player, reply, prefix + reply);
    }

    private void handleShopPurchase(ServerPlayer player, int offerId, String prefix) {
        int cost = switch (offerId) {
            case 0 -> 3;
            case 1 -> 4;
            case 2 -> 5;
            case 3 -> 6;
            case 4 -> 8;
            case 5 -> 8;
            case 6 -> 10;
            case 7 -> 15;
            default -> -1;
        };
        if (cost < 0) {
            return;
        }

        Item currency = ModItems.CORRUPTED_BIT.get();
        if (!hasItems(player, currency, cost)) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            String err = "§cNecesitas " + cost + " Bits Corruptos para este intercambio.";
            sendReply(player, err, prefix + err);
            return;
        }

        consumeItems(player, currency, cost);
        String boughtLabel;
        switch (offerId) {
            case 0 -> {
                giveOrDrop(player, new ItemStack(Items.GOLDEN_APPLE, 4));
                giveOrDrop(player, new ItemStack(Items.COOKED_BEEF, 8));
                boughtLabel = "4 Manzanas Doradas y 8 Filetes";
            }
            case 1 -> {
                giveOrDrop(player, new ItemStack(Items.EXPERIENCE_BOTTLE, 8));
                boughtLabel = "8 Botellas de Experiencia";
            }
            case 2 -> {
                giveOrDrop(player, new ItemStack(ModItems.SYSTEM_LOG.get(), 1));
                boughtLabel = "1 Registro del Sistema (Lore)";
            }
            case 3 -> {
                giveOrDrop(player, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
                boughtLabel = "1 Tótem de la Inmortalidad";
            }
            case 4 -> {
                giveOrDrop(player, new ItemStack(Items.REDSTONE_BLOCK, 1));
                boughtLabel = "1 Bloque de Redstone";
            }
            case 5 -> {
                giveOrDrop(player, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
                boughtLabel = "1 Manzana Dorada Encantada";
            }
            case 6 -> {
                Item firewallPiece = pickNextArmorPiece(player, true);
                giveOrDrop(player, new ItemStack(firewallPiece, 1));
                boughtLabel = firewallPiece.getDescription().getString();
            }
            default -> {
                Item quantumPiece = pickNextArmorPiece(player, false);
                giveOrDrop(player, new ItemStack(quantumPiece, 1));
                boughtLabel = quantumPiece.getDescription().getString();
            }
        }

        player.containerMenu.broadcastChanges();
        player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        String ok = "§a¡Intercambio realizado! Recibiste: §e" + boughtLabel;
        sendReply(player, ok, prefix + ok);
    }

    private Item pickNextArmorPiece(ServerPlayer player, boolean firewall) {
        Item[] pieces = firewall ? new Item[]{
                ModItems.FIREWALL_HELMET.get(),
                ModItems.FIREWALL_CHESTPLATE.get(),
                ModItems.FIREWALL_LEGGINGS.get(),
                ModItems.FIREWALL_BOOTS.get()
        } : new Item[]{
                ModItems.QUANTUM_HELMET.get(),
                ModItems.QUANTUM_CHESTPLATE.get(),
                ModItems.QUANTUM_LEGGINGS.get(),
                ModItems.QUANTUM_BOOTS.get()
        };
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        List<Item> missing = new ArrayList<>();
        for (int i = 0; i < pieces.length; i++) {
            Item piece = pieces[i];
            boolean equipped = player.getItemBySlot(slots[i]).is(piece);
            if (!equipped && !hasItems(player, piece, 1)) {
                missing.add(piece);
            }
        }

        if (!missing.isEmpty()) {
            return missing.get(player.getRandom().nextInt(missing.size()));
        }
        return pieces[player.getRandom().nextInt(pieces.length)];
    }

    private void checkQuestCompletion(ServerPlayer player, int stage, String prefix) {
        if (stage >= 4) {
            return;
        }

        Item item = ModItems.CORRUPTED_BIT.get();
        int amount = switch (stage) {
            case 0 -> 5;
            case 1 -> 10;
            case 2 -> 15;
            case 3 -> 20;
            default -> 0;
        };

        if (hasItems(player, item, amount)) {
            consumeItems(player, item, amount);
            RpgStats.setQuestActive(player, false);
            RpgStats.setQuestStage(player, stage + 1);
            this.setQuestActive(false);
            this.setQuestStage(stage + 1);

            giveOrDrop(player, new ItemStack(ModItems.BOSS_FRAGMENT.get(), 1));

            String guiMsg;
            if (stage == 0) {
                giveOrDrop(player, new ItemStack(Items.EXPERIENCE_BOTTLE, 5));
                giveOrDrop(player, new ItemStack(Items.GOLDEN_APPLE, 4));
                showTitle(player, "¡Capítulo 1 Completado!", ChatFormatting.GREEN);
                guiMsg = "§a¡Capítulo 1 completado! Recibiste tu 1er Fragmento + XP + Manzanas. Acepta el Capítulo 2 cuando quieras.";
            } else if (stage == 1) {
                giveOrDrop(player, new ItemStack(ModItems.FIREWALL_HELMET.get()));
                giveOrDrop(player, new ItemStack(ModItems.FIREWALL_CHESTPLATE.get()));
                giveOrDrop(player, new ItemStack(ModItems.FIREWALL_LEGGINGS.get()));
                giveOrDrop(player, new ItemStack(ModItems.FIREWALL_BOOTS.get()));
                showTitle(player, "¡Capítulo 2 Completado!", ChatFormatting.GREEN);
                guiMsg = "§a¡Capítulo 2 completado! Recibiste tu 2do Fragmento y el §9Set Cortafuegos completo§a.";
            } else if (stage == 2) {
                giveOrDrop(player, new ItemStack(ModItems.GLITCH_SWORD.get(), 1));
                giveOrDrop(player, new ItemStack(ModItems.SYSTEM_LOG.get(), 1));
                showTitle(player, "¡Capítulo 3 Completado!", ChatFormatting.GREEN);
                guiMsg = "§a¡Capítulo 3 completado! Recibiste tu 3er Fragmento, la §dEspada Glitch §ay un Registro del Sistema.";
            } else {
                ServerLevel serverLevel = player.serverLevel();
                BlockPos towerPos = StructureMapItem.findOrLocatePyramid(serverLevel, player.blockPosition());
                if (towerPos != null) {
                    RpgStats.setPyramidPos(player, towerPos);
                    giveOrDrop(player, StructureMapItem.createBoundMap(towerPos));

                    ItemStack vanillaMap = MapItem.create(serverLevel, towerPos.getX(), towerPos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap(serverLevel, vanillaMap);
                    MapItemSavedData.addTargetDecoration(vanillaMap, towerPos, "+", MapDecorationTypes.TARGET_X);
                    vanillaMap.set(DataComponents.CUSTOM_NAME, Component.literal("§6Mapa Explorador: Torre de Base de Datos"));
                    giveOrDrop(player, vanillaMap);

                    player.getPersistentData().putBoolean("RpgStoryCompleted", true);
                    showTitle(player, "¡Torre Localizada!", ChatFormatting.GOLD);
                    showSubtitle(player, "Torre en X=" + towerPos.getX() + ", Z=" + towerPos.getZ());
                    guiMsg = "§6¡Capítulo Final completado! §eTorre en X=" + towerPos.getX() + ", Y=" + towerPos.getY() + ", Z=" + towerPos.getZ()
                            + ". Lleva tus 4 Fragmentos + 1 Bloque de Redstone a los Prisioneros adentro para compilar la Key.";
                    player.sendSystemMessage(Component.literal(
                            "§6[Historia] §b¡Torre de Base de Datos localizada en §fX="
                                    + towerPos.getX() + ", Y=" + towerPos.getY() + ", Z=" + towerPos.getZ()
                                    + "§b! Adentro encontrarás prisioneros en jaulas capaces de compilar tu §6Código de Invocación Corrupto§b."));
                } else {
                    player.getPersistentData().putBoolean("RpgStoryCompleted", true);
                    showTitle(player, "¡Capítulo Final Completado!", ChatFormatting.GOLD);
                    guiMsg = "§c[!] No se detectó ninguna Torre generada en este mundo (¿estructuras desactivadas?).";
                    player.sendSystemMessage(Component.literal(guiMsg));
                }
            }

            player.containerMenu.broadcastChanges();
            ModMessages.syncPlayerToSelf(player, false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            sendReply(player, guiMsg, null);
        } else {
            String err = "§cAún te faltan Bits Corruptos (" + countItems(player, item) + "/" + amount + "). ¡Derrota más Secuaces Corruptos!";
            sendReply(player, err, prefix + err);
        }
    }

    private void sendReply(ServerPlayer player, String guiText, String chatText) {
        ModMessages.sendToPlayer(new NpcDialoguePayload(this.getId(), guiText), player);
        if (chatText != null && !chatText.isEmpty()) {
            player.sendSystemMessage(Component.literal(chatText));
        }
    }

    private void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void showTitle(ServerPlayer player, String text, ChatFormatting color) {
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(text).withStyle(color)));
    }

    private void showSubtitle(ServerPlayer player, String text) {
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(text)));
    }

    private int countItems(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    private boolean hasItems(Player player, Item item, int amount) {
        return countItems(player, item) >= amount;
    }

    private void consumeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (stack.getCount() >= remaining) {
                    stack.shrink(remaining);
                    break;
                } else {
                    remaining -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
    }

    private String getRequirementString(int stage) {
        return switch (stage) {
            case 0 -> "5 Bits Corruptos";
            case 1 -> "10 Bits Corruptos";
            case 2 -> "15 Bits Corruptos";
            case 3 -> "20 Bits Corruptos";
            default -> "Nada";
        };
    }
}
