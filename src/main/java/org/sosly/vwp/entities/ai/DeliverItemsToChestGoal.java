package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.List;

public class DeliverItemsToChestGoal extends Goal {
    private final Porter porter;
    private static final long DELIVERY_TIME = 40L;
    private long deliveryStartTime;
    
    public DeliverItemsToChestGoal(Porter porter) {
        this.porter = porter;
        this.deliveryStartTime = 0;
    }
    
    @Override
    public boolean canUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.DELIVERING_ITEMS)) {
            return false;
        }
        
        return task.isChestOpened() && task.getOpenContainer() != null && !task.getItemsToDeliver().isEmpty();
    }
    
    @Override
    public boolean canContinueToUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.DELIVERING_ITEMS)) {
            return false;
        }
        
        long currentTime = porter.level().getGameTime();
        return currentTime - deliveryStartTime < DELIVERY_TIME;
    }
    
    @Override
    public void start() {
        deliveryStartTime = porter.level().getGameTime();
        
        DeliveryTask task = porter.getDeliveryTask();
        Container workerChest = task.getOpenContainer();
        AbstractWorkerEntity targetWorker = findTargetWorker();
        
        if (workerChest == null || targetWorker == null) {
            task.reset();
            return;
        }

        deliverItems(workerChest, task.getItemsToDeliver());
        porter.chat(Component.translatable("chat.vwp.porter.delivered_items", targetWorker.getName()));
    }
    
    @Override
    public void stop() {
        DeliveryTask task = porter.getDeliveryTask();
        task.getItemsToDeliver().clear();
        task.transitionTo(DeliveryTask.State.RETURNING_HOME);
        deliveryStartTime = 0;
    }
    
    private void deliverItems(Container workerChest, List<ItemStack> itemsToDeliver) {
        SimpleContainer porterInv = porter.getInventory();
        
        for (ItemStack neededItem : itemsToDeliver) {
            transferItemToChest(porterInv, workerChest, neededItem);
        }
    }

    private AbstractWorkerEntity findTargetWorker() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task.getTargetWorker() == null) {
            return null;
        }

        List<AbstractWorkerEntity> nearbyWorkers = porter.level().getEntitiesOfClass(
                AbstractWorkerEntity.class,
                porter.getBoundingBox().inflate(CommonConfig.porterScanRadius * 2),
                worker -> worker.getUUID().equals(task.getTargetWorker().getId())
        );

        return nearbyWorkers.isEmpty() ? null : nearbyWorkers.get(0);
    }
    
    private void transferItemToChest(SimpleContainer porterInv, Container workerChest, ItemStack neededItem) {
        for (int i = 0; i < porterInv.getContainerSize(); i++) {
            ItemStack item = porterInv.getItem(i);
            if (item.isEmpty() || !ItemStack.isSameItemSameTags(item, neededItem)) {
                continue;
            }
            
            ItemStack toTransfer = item.copy();
            toTransfer.setCount(Math.min(item.getCount(), neededItem.getCount()));
            
            int transferred = stackIntoExistingSlots(workerChest, toTransfer);
            item.shrink(transferred);
            toTransfer.shrink(transferred);
            
            if (!toTransfer.isEmpty()) {
                transferred = placeInEmptySlot(workerChest, toTransfer);
                item.shrink(transferred);
            }
            
            if (item.isEmpty()) {
                porterInv.setItem(i, ItemStack.EMPTY);
            }
            break;
        }
    }
    
    private int stackIntoExistingSlots(Container chest, ItemStack toTransfer) {
        int totalTransferred = 0;
        
        for (int j = 0; j < chest.getContainerSize(); j++) {
            ItemStack existing = chest.getItem(j);
            if (!ItemStack.isSameItemSameTags(existing, toTransfer)) {
                continue;
            }
            
            int space = existing.getMaxStackSize() - existing.getCount();
            if (space <= 0) {
                continue;
            }
            
            int toAdd = Math.min(space, toTransfer.getCount() - totalTransferred);
            existing.grow(toAdd);
            totalTransferred += toAdd;
            
            if (totalTransferred >= toTransfer.getCount()) {
                break;
            }
        }
        
        return totalTransferred;
    }
    
    private int placeInEmptySlot(Container chest, ItemStack toPlace) {
        for (int j = 0; j < chest.getContainerSize(); j++) {
            if (!chest.getItem(j).isEmpty()) {
                continue;
            }
            
            chest.setItem(j, toPlace.copy());
            return toPlace.getCount();
        }
        return 0;
    }
}
