package org.sosly.vwp.entities.workers;

import com.talhanation.workers.CommandEvents;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import org.sosly.vwp.data.WorkerInfo;
import org.sosly.vwp.entities.ai.MeetNewWorkerGoal;
import org.sosly.vwp.entities.ai.CheckKnownWorkersGoal;
import org.sosly.vwp.gui.providers.HireProvider;
import org.sosly.vwp.gui.providers.WorkerProvider;
import org.sosly.vwp.networking.PacketHandler;
import org.sosly.vwp.networking.clientbound.UpdateHireScreen;
import org.sosly.vwp.networking.serverbound.OpenHireGUI;
import org.sosly.vwp.networking.serverbound.OpenWorkerGUI;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Porter extends AbstractWorkerEntity {
    private final Map<UUID, WorkerInfo> knownWorkers = new HashMap<>();
    
    public Porter(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
        this.setProfessionName("Porter");
        this.cost = 20;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        
        // Save known workers
        CompoundTag knownWorkersTag = new CompoundTag();
        knownWorkersTag.putInt("count", knownWorkers.size());
        int index = 0;
        for (WorkerInfo info : knownWorkers.values()) {
            knownWorkersTag.put("worker_" + index, info.save());
            index++;
        }
        nbt.put("KnownWorkers", knownWorkersTag);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        
        // Load known workers
        if (nbt.contains("KnownWorkers")) {
            CompoundTag knownWorkersTag = nbt.getCompound("KnownWorkers");
            int count = knownWorkersTag.getInt("count");
            knownWorkers.clear();
            for (int i = 0; i < count; i++) {
                if (knownWorkersTag.contains("worker_" + i)) {
                    WorkerInfo info = WorkerInfo.load(knownWorkersTag.getCompound("worker_" + i));
                    knownWorkers.put(info.getId(), info);
                }
            }
        }
    }

    @Override
    public boolean canWorkWithoutTool() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new MeetNewWorkerGoal(this));
        this.goalSelector.addGoal(10, new CheckKnownWorkersGoal(this));
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
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
    
    public boolean knowsWorker(UUID workerId) {
        return knownWorkers.containsKey(workerId);
    }
    
    public void addKnownWorker(UUID workerId, String workerName, BlockPos workPosition) {
        WorkerInfo previousInfo = knownWorkers.get(workerId);
        if (previousInfo == null) {
            knownWorkers.put(workerId, new WorkerInfo(workerId, workerName, workPosition));
            VillageWorkersPlus.LOGGER.info("Porter {} successfully met and added worker {} ({}) to known list (total known: {})", 
                this.getUUID(), workerName, workerId, knownWorkers.size());
        } else {
            // Update name and position if changed
            if (!previousInfo.getName().equals(workerName)) {
                VillageWorkersPlus.LOGGER.info("Porter {} updated name for worker {} from '{}' to '{}'", 
                    this.getUUID(), workerId, previousInfo.getName(), workerName);
                previousInfo.setName(workerName);
            }
            if (workPosition != null && !workPosition.equals(previousInfo.getWorkPosition())) {
                previousInfo.setWorkPosition(workPosition);
            }
        }
    }
    
    public void removeKnownWorker(UUID workerId) {
        WorkerInfo info = knownWorkers.remove(workerId);
        if (info != null) {
            VillageWorkersPlus.LOGGER.info("Porter {} removed worker {} ({}) from known list (total known: {})", 
                this.getUUID(), info.getName(), workerId, knownWorkers.size());
        }
    }
    
    public String getKnownWorkerName(UUID workerId) {
        WorkerInfo info = knownWorkers.get(workerId);
        return info != null ? info.getName() : "someone";
    }
    
    public WorkerInfo getWorkerInfo(UUID workerId) {
        return knownWorkers.get(workerId);
    }
    
    public List<WorkerInfo> getAllKnownWorkers() {
        return new ArrayList<>(knownWorkers.values());
    }
    
    public Set<UUID> getKnownWorkers() {
        return new HashSet<>(knownWorkers.keySet());
    }
    
    public int getKnownWorkerCount() {
        return knownWorkers.size();
    }
}
