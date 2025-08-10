package org.sosly.vwp.tasks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.sosly.vwp.data.WorkerInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeliveryTask extends AbstractTask {
    public enum State {
        IDLE,
        SELECTING_WORKER,
        NAVIGATING_TO_WORKER,
        ASSESSING_NEEDS,
        GOING_TO_OWN_CHEST,
        COLLECTING_ITEMS,
        GOING_TO_WORKER_CHEST,
        DELIVERING_ITEMS,
        RETURNING_HOME
    }
    
    private State currentState = State.IDLE;
    private WorkerInfo targetWorker;
    private UUID targetWorkerId;
    private BlockPos targetWorkerChestPos;
    private BlockPos ownChestPos;
    private BlockPos homePos;
    private List<ItemStack> itemsToDeliver = new ArrayList<>();
    private Container openContainer;
    private boolean chestOpened;
    private int attemptCount;
    private final Map<UUID, Long> lastVisitTimes = new HashMap<>();
    
    public DeliveryTask() {
        super();
        reset();
    }
    
    public void transitionTo(State newState) {
        this.currentState = newState;
        this.stateStartTime = 0;
    }
    
    public boolean isInState(State state) {
        return currentState == state;
    }
    
    public State getCurrentState() {
        return currentState;
    }
    
    public WorkerInfo getTargetWorker() {
        return targetWorker;
    }
    
    public void setTargetWorker(WorkerInfo worker) {
        this.targetWorker = worker;
        this.targetWorkerId = worker != null ? worker.getId() : null;
    }
    
    public BlockPos getTargetWorkerChestPos() {
        return targetWorkerChestPos;
    }
    
    public void setTargetWorkerChestPos(BlockPos pos) {
        this.targetWorkerChestPos = pos;
    }
    
    public BlockPos getOwnChestPos() {
        return ownChestPos;
    }
    
    public void setOwnChestPos(BlockPos pos) {
        this.ownChestPos = pos;
    }
    
    public BlockPos getHomePos() {
        return homePos;
    }
    
    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }
    
    public List<ItemStack> getItemsToDeliver() {
        return itemsToDeliver;
    }
    
    public void setItemsToDeliver(List<ItemStack> items) {
        this.itemsToDeliver.clear();
        if (items != null) {
            this.itemsToDeliver.addAll(items);
        }
    }
    
    public Container getOpenContainer() {
        return openContainer;
    }
    
    public void setOpenContainer(Container container) {
        this.openContainer = container;
    }
    
    public boolean isChestOpened() {
        return chestOpened;
    }
    
    public void setChestOpened(boolean opened) {
        this.chestOpened = opened;
    }
    
    public int getAttemptCount() {
        return attemptCount;
    }
    
    public void incrementAttemptCount() {
        this.attemptCount++;
    }
    
    public void resetAttemptCount() {
        this.attemptCount = 0;
    }
    
    public void recordVisit(UUID workerId, long gameTime) {
        lastVisitTimes.put(workerId, gameTime);
    }
    
    public long getLastVisitTime(UUID workerId) {
        return lastVisitTimes.getOrDefault(workerId, 0L);
    }
    
    public boolean wasRecentlyVisited(UUID workerId, long currentTime, long cooldownTicks) {
        long lastVisit = getLastVisitTime(workerId);
        return (currentTime - lastVisit) < cooldownTicks;
    }
    
    @Override
    public void reset() {
        currentState = State.IDLE;
        targetWorker = null;
        targetWorkerId = null;
        targetWorkerChestPos = null;
        ownChestPos = null;
        homePos = null;
        itemsToDeliver.clear();
        openContainer = null;
        chestOpened = false;
        attemptCount = 0;
        stateStartTime = 0;
        lastUpdateTime = 0;
    }
    
    @Override
    public void save(CompoundTag tag) {
        saveBaseData(tag);
        tag.putString("CurrentState", currentState.name());
        
        if (targetWorkerId != null) {
            tag.putUUID("TargetWorkerId", targetWorkerId);
        }
        
        if (targetWorkerChestPos != null) {
            tag.put("TargetWorkerChestPos", NbtUtils.writeBlockPos(targetWorkerChestPos));
        }
        
        if (ownChestPos != null) {
            tag.put("OwnChestPos", NbtUtils.writeBlockPos(ownChestPos));
        }
        
        if (homePos != null) {
            tag.put("HomePos", NbtUtils.writeBlockPos(homePos));
        }
        
        ListTag itemsList = new ListTag();
        for (ItemStack stack : itemsToDeliver) {
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                itemsList.add(itemTag);
            }
        }
        tag.put("ItemsToDeliver", itemsList);
        
        tag.putBoolean("ChestOpened", chestOpened);
        tag.putInt("AttemptCount", attemptCount);
        
        CompoundTag visitsTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : lastVisitTimes.entrySet()) {
            visitsTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("LastVisitTimes", visitsTag);
    }
    
    @Override
    public void load(CompoundTag tag) {
        loadBaseData(tag);
        
        String stateName = tag.getString("CurrentState");
        try {
            currentState = State.valueOf(stateName);
        } catch (IllegalArgumentException e) {
            currentState = State.IDLE;
        }
        
        if (tag.hasUUID("TargetWorkerId")) {
            targetWorkerId = tag.getUUID("TargetWorkerId");
        }
        
        if (tag.contains("TargetWorkerChestPos")) {
            targetWorkerChestPos = NbtUtils.readBlockPos(tag.getCompound("TargetWorkerChestPos"));
        }
        
        if (tag.contains("OwnChestPos")) {
            ownChestPos = NbtUtils.readBlockPos(tag.getCompound("OwnChestPos"));
        }
        
        if (tag.contains("HomePos")) {
            homePos = NbtUtils.readBlockPos(tag.getCompound("HomePos"));
        }
        
        itemsToDeliver.clear();
        ListTag itemsList = tag.getList("ItemsToDeliver", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            if (!stack.isEmpty()) {
                itemsToDeliver.add(stack);
            }
        }
        
        chestOpened = tag.getBoolean("ChestOpened");
        attemptCount = tag.getInt("AttemptCount");
        
        lastVisitTimes.clear();
        if (tag.contains("LastVisitTimes")) {
            CompoundTag visitsTag = tag.getCompound("LastVisitTimes");
            for (String key : visitsTag.getAllKeys()) {
                try {
                    UUID workerId = UUID.fromString(key);
                    long visitTime = visitsTag.getLong(key);
                    lastVisitTimes.put(workerId, visitTime);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }
}