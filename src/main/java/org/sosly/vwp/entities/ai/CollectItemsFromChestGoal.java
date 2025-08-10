package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.ArrayList;
import java.util.List;

public class CollectItemsFromChestGoal extends Goal {
    private final Porter porter;
    private static final long COLLECTION_TIME = 40L;
    private long collectionStartTime;
    
    public CollectItemsFromChestGoal(Porter porter) {
        this.porter = porter;
        this.collectionStartTime = 0;
    }
    
    @Override
    public boolean canUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.COLLECTING_ITEMS)) {
            return false;
        }
        
        return task.isChestOpened() && task.getOpenContainer() != null;
    }
    
    @Override
    public boolean canContinueToUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.COLLECTING_ITEMS)) {
            return false;
        }
        
        long currentTime = porter.level().getGameTime();
        return currentTime - collectionStartTime < COLLECTION_TIME;
    }
    
    @Override
    public void start() {
        collectionStartTime = porter.level().getGameTime();
        
        DeliveryTask task = porter.getDeliveryTask();
        Container chest = task.getOpenContainer();
        
        if (chest == null) {
            porter.chat(Component.translatable("chat.vwp.porter.no_chest"));
            task.reset();
            return;
        }

        AbstractWorkerEntity targetWorker = findTargetWorker();
        if (targetWorker == null) {
            task.reset();
            return;
        }
        
        List<ItemStack> collected = collectNeededItems(chest, targetWorker);
        task.setItemsToDeliver(collected);

        if (collected.isEmpty()) {
            porter.chat(Component.translatable("chat.vwp.porter.no_items_for_worker", targetWorker.getName()));
            task.reset();
            return;
        }

        porter.chat(Component.translatable("chat.vwp.porter.found_items_for_worker", targetWorker.getName()));
    }
    
    @Override
    public void stop() {
        DeliveryTask task = porter.getDeliveryTask();

        if (task.getItemsToDeliver().isEmpty()) {
            return;
        }

        task.transitionTo(DeliveryTask.State.GOING_TO_WORKER_CHEST);
        collectionStartTime = 0;
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
    
    private List<ItemStack> collectNeededItems(Container chest, AbstractWorkerEntity targetWorker) {
        List<ItemStack> collected = new ArrayList<>();
        
        if (workerNeedsFood(targetWorker)) {
            collectFood(chest, collected);
        }
        
        if (targetWorker.needsMainTool && targetWorker.hasAMainTool()) {
            collectMainTool(chest, targetWorker, collected);
        }
        
        if (targetWorker.needsSecondTool && targetWorker.hasASecondTool()) {
            collectSecondTool(chest, targetWorker, collected);
        }
        
        return collected;
    }
    
    private boolean workerNeedsFood(AbstractWorkerEntity worker) {
        if (worker.needsToEat()) {
            return true;
        }
        
        int totalFood = countFoodInInventory(worker);
        if (totalFood >= 3) {
            return false;
        }
        
        BlockPos workerChestPos = findWorkerChest(worker);
        if (workerChestPos == null) {
            return true;
        }
        
        Container workerChest = getContainer(workerChestPos);
        if (workerChest == null) {
            return true;
        }
        
        totalFood += countFoodInContainer(workerChest);
        return totalFood < 3;
    }
    
    private void collectFood(Container chest, List<ItemStack> collected) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack item = chest.getItem(i);
            if (!item.isEdible() || item.getFoodProperties(porter).getNutrition() <= 4) {
                continue;
            }
            
            int amount = Math.min(16, item.getCount());
            ItemStack taken = item.copy();
            taken.setCount(amount);
            porter.getInventory().addItem(taken);
            collected.add(taken.copy());
            item.shrink(amount);
            return;
        }
    }
    
    private void collectMainTool(Container chest, AbstractWorkerEntity targetWorker, List<ItemStack> collected) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack item = chest.getItem(i);
            if (!targetWorker.isRequiredMainTool(item)) {
                continue;
            }
            
            ItemStack taken = item.copy();
            taken.setCount(1);
            porter.getInventory().addItem(taken);
            collected.add(taken.copy());
            item.shrink(1);
            return;
        }
    }
    
    private void collectSecondTool(Container chest, AbstractWorkerEntity targetWorker, List<ItemStack> collected) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack item = chest.getItem(i);
            if (!targetWorker.isRequiredSecondTool(item)) {
                continue;
            }
            
            ItemStack taken = item.copy();
            taken.setCount(1);
            porter.getInventory().addItem(taken);
            collected.add(taken.copy());
            item.shrink(1);
            return;
        }
    }
    
    private int countFoodInInventory(AbstractWorkerEntity worker) {
        int foodCount = 0;
        SimpleContainer inv = worker.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item.isEdible() && item.getFoodProperties(worker).getNutrition() > 4) {
                foodCount += item.getCount();
            }
        }
        return foodCount;
    }
    
    private int countFoodInContainer(Container container) {
        int foodCount = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (item.isEdible() && item.getFoodProperties(porter).getNutrition() > 4) {
                foodCount += item.getCount();
            }
        }
        return foodCount;
    }
    
    private BlockPos findWorkerChest(AbstractWorkerEntity worker) {
        BlockPos workerChest = worker.getChestPos();
        if (workerChest != null) {
            return workerChest;
        }
        
        BlockPos workPos = worker.getStartPos();
        if (workPos == null) {
            return null;
        }
        
        for (BlockPos pos : BlockPos.betweenClosed(
            workPos.offset(-2, -2, -2),
            workPos.offset(2, 2, 2))) {
            BlockEntity entity = porter.level().getBlockEntity(pos);
            if (entity instanceof ChestBlockEntity || entity instanceof Container) {
                return pos;
            }
        }
        return null;
    }
    
    private Container getContainer(BlockPos chestPos) {
        BlockEntity entity = porter.level().getBlockEntity(chestPos);
        BlockState blockState = porter.level().getBlockState(chestPos);
        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, blockState, porter.level(), chestPos, false);
        }
        if (entity instanceof Container containerEntity) {
            return containerEntity;
        }
        return null;
    }
}
