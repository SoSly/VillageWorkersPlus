package org.sosly.vwp.entities.workers;

import com.talhanation.workers.CommandEvents;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.data.WorkerInfo;
import org.sosly.vwp.entities.ai.*;
import org.sosly.vwp.tasks.DeliveryTask;
import org.sosly.vwp.gui.providers.HireProvider;
import org.sosly.vwp.gui.providers.WorkerProvider;
import org.sosly.vwp.networking.PacketHandler;
import org.sosly.vwp.networking.clientbound.UpdateHireScreen;
import org.sosly.vwp.networking.serverbound.OpenHireGUI;
import org.sosly.vwp.networking.serverbound.OpenWorkerGUI;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Porter extends AbstractWorkerEntity {
    private final Map<UUID, WorkerInfo> knownWorkers = new HashMap<>();
    private final DeliveryTask deliveryTask = new DeliveryTask();
    
    public Porter(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
        this.setProfessionName("Porter");
        this.cost = 20;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        
        CompoundTag knownWorkersTag = new CompoundTag();
        knownWorkersTag.putInt("count", knownWorkers.size());
        int index = 0;
        for (WorkerInfo info : knownWorkers.values()) {
            knownWorkersTag.put("worker_" + index, info.save());
            index++;
        }
        nbt.put("KnownWorkers", knownWorkersTag);
        
        CompoundTag taskTag = new CompoundTag();
        deliveryTask.save(taskTag);
        nbt.put("DeliveryTask", taskTag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        if (!nbt.contains("KnownWorkers")) {
            VillageWorkersPlus.LOGGER.warn("Porter {} has no known workers data in NBT, initializing empty list.", this.getUUID());
            return;
        }

        CompoundTag knownWorkersTag = nbt.getCompound("KnownWorkers");
        int count = knownWorkersTag.getInt("count");
        knownWorkers.clear();
        for (int i = 0; i < count; i++) {
            if (knownWorkersTag.contains("worker_" + i)) {
                WorkerInfo info = WorkerInfo.load(knownWorkersTag.getCompound("worker_" + i));
                knownWorkers.put(info.getId(), info);
            }
        }
        
        if (nbt.contains("DeliveryTask")) {
            deliveryTask.load(nbt.getCompound("DeliveryTask"));
        }
    }

    @Override
    public boolean canWorkWithoutTool() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new MeetNewWorkerGoal(this));
        this.goalSelector.addGoal(7, new SelectKnownWorkerGoal(this));
        
        this.goalSelector.addGoal(6, new NavigateToPositionGoal(this,
            () -> {
                if (!deliveryTask.isInState(DeliveryTask.State.NAVIGATING_TO_WORKER)) {
                    return null;
                }
                WorkerInfo targetWorker = deliveryTask.getTargetWorker();
                return targetWorker != null ? targetWorker.getWorkPosition() : null;
            },
            CommonConfig.porterChatDistance,
            () -> deliveryTask.isInState(DeliveryTask.State.NAVIGATING_TO_WORKER)));
        
        this.goalSelector.addGoal(6, new AssessWorkerNeedsGoal(this));
        
        this.goalSelector.addGoal(5, new NavigateToPositionGoal(this,
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_OWN_CHEST) ? deliveryTask.getOwnChestPos() : null,
            3.0,
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_OWN_CHEST)));
        
        this.goalSelector.addGoal(5, new OpenChestGoal(this,
            () -> deliveryTask.getOwnChestPos(),
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_OWN_CHEST) && !deliveryTask.isChestOpened(),
            () -> deliveryTask.transitionTo(DeliveryTask.State.COLLECTING_ITEMS)));
        
        this.goalSelector.addGoal(5, new CollectItemsFromChestGoal(this));
        
        this.goalSelector.addGoal(5, new CloseChestGoal(this,
            () -> deliveryTask.getOwnChestPos(),
            () -> deliveryTask.isInState(DeliveryTask.State.COLLECTING_ITEMS) && deliveryTask.isChestOpened(),
            null));
        
        this.goalSelector.addGoal(4, new NavigateToPositionGoal(this,
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_WORKER_CHEST) ? deliveryTask.getTargetWorkerChestPos() : null,
            3.0,
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_WORKER_CHEST)));
        
        this.goalSelector.addGoal(4, new OpenChestGoal(this,
            () -> deliveryTask.getTargetWorkerChestPos(),
            () -> deliveryTask.isInState(DeliveryTask.State.GOING_TO_WORKER_CHEST) && !deliveryTask.isChestOpened(),
            () -> deliveryTask.transitionTo(DeliveryTask.State.DELIVERING_ITEMS)));
        
        this.goalSelector.addGoal(4, new DeliverItemsToChestGoal(this));
        
        this.goalSelector.addGoal(4, new CloseChestGoal(this,
            () -> deliveryTask.getTargetWorkerChestPos(),
            () -> deliveryTask.isInState(DeliveryTask.State.DELIVERING_ITEMS) && deliveryTask.isChestOpened(),
            null));
        
        this.goalSelector.addGoal(3, new ReturnHomeGoal(this,
            () -> this.getStartPos(),
            () -> deliveryTask.isInState(DeliveryTask.State.RETURNING_HOME),
            null));
    }

    @Override
    public boolean isRequiredMainTool(ItemStack tool) {
        return false;
    }

    @Override
    public boolean isRequiredSecondTool(ItemStack tool) {
        return false;
    }

    @Override
    public List<Item> inventoryInputHelp() {
        return null;
    }

    @Override
    public boolean hasAMainTool() {
        return false;
    }

    @Override
    public boolean hasASecondTool() {
        return false;
    }

    @Override
    public Predicate<ItemEntity> getAllowedItems() {
        return null;
    }

    @Override
    public void openHireGUI(Player player) {
        this.navigation.stop();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            PacketHandler.network.sendToServer(new OpenHireGUI(player, this.getUUID()));
            return;
        }

        PacketHandler.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new UpdateHireScreen(CommandEvents.getWorkersCurrency(), this.getWorkerCost()));
        Consumer<FriendlyByteBuf> extraDataWriter = (packetBuffer) -> packetBuffer.writeUUID(this.getUUID());
        NetworkHooks.openScreen(serverPlayer, new HireProvider<Porter>(Porter.this), extraDataWriter);
    }

    @Override
    public void openGUI(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            VillageWorkersPlus.LOGGER.info("Porter inventory GUI opened on client side, sending to server.");
            PacketHandler.network.sendToServer(new OpenWorkerGUI(player, this.getUUID()));
            return;
        }

        Consumer<FriendlyByteBuf> extraDataWriter = (packetBuffer) -> packetBuffer.writeUUID(this.getUUID());
        NetworkHooks.openScreen(serverPlayer, new WorkerProvider<Porter>(Porter.this), extraDataWriter);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        return null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractWorkerEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
    
    public boolean knowsWorker(UUID workerId) {
        return knownWorkers.containsKey(workerId);
    }
    
    public void addKnownWorker(UUID workerId, String workerName, BlockPos workPosition) {
        WorkerInfo previousInfo = knownWorkers.get(workerId);

        if (previousInfo == null) {
            knownWorkers.put(workerId, new WorkerInfo(workerId, workerName, workPosition));
            return;
        }

        if (!previousInfo.getName().equals(workerName)) {
            VillageWorkersPlus.LOGGER.info("Porter {} updated name for worker {} from '{}' to '{}'",
                this.getUUID(), workerId, previousInfo.getName(), workerName);
            previousInfo.setName(workerName);
        }

        if (workPosition != null && !workPosition.equals(previousInfo.getWorkPosition())) {
            previousInfo.setWorkPosition(workPosition);
        }
    }
    
    public void removeKnownWorker(UUID workerId) {
        knownWorkers.remove(workerId);
    }

    public List<WorkerInfo> getAllKnownWorkers() {
        return new ArrayList<>(knownWorkers.values());
    }
    
    public List<WorkerInfo> getKnownWorkers() {
        return getAllKnownWorkers();
    }
    
    public DeliveryTask getDeliveryTask() {
        return deliveryTask;
    }

    public void chat(Component message) {
        if (CommonConfig.porterIsChatty) {
            tellPlayer(getOwner(), message);
        }
    }

    public double getMovementSpeed() {
        return Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).getValue();
    }
}
