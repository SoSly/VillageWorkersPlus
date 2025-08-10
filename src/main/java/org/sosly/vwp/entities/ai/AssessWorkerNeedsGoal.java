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

public class AssessWorkerNeedsGoal extends Goal {
    private final Porter porter;
    private static final long WAIT_TIME = 60L;
    private long assessmentStartTime;
    public AssessWorkerNeedsGoal(Porter porter) {
        this.porter = porter;
        this.assessmentStartTime = 0;
    }
    
    @Override
    public boolean canUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.ASSESSING_NEEDS)) {
            return false;
        }
        
        return task.getTargetWorker() != null;
    }
    
    @Override
    public boolean canContinueToUse() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.ASSESSING_NEEDS)) {
            return false;
        }

        AbstractWorkerEntity targetWorker = findTargetWorker();
        if (targetWorker == null) {
            task.reset();
            return false;
        }
        
        long currentTime = porter.level().getGameTime();
        return currentTime - assessmentStartTime < WAIT_TIME;
    }
    
    @Override
    public void start() {
        assessmentStartTime = porter.level().getGameTime();
        porter.getNavigation().stop();
        
        DeliveryTask task = porter.getDeliveryTask();
        AbstractWorkerEntity targetWorker = findTargetWorker();
        
        if (task.getTargetWorker() != null) {
            task.recordVisit(task.getTargetWorker().getId(), assessmentStartTime);
        }
        
        if (targetWorker == null) {
            task.reset();
        }
    }
    
    @Override
    public void stop() {
        DeliveryTask task = porter.getDeliveryTask();

        if (task == null || !task.isInState(DeliveryTask.State.ASSESSING_NEEDS)) {
            return;
        }

        AbstractWorkerEntity targetWorker = findTargetWorker();
        if (targetWorker == null) {
            task.reset();
            return;
        }

        if (!checkWorkerNeeds(targetWorker)) {
            task.reset();
            return;
        }

        String needs = describeNeeds(targetWorker);
        porter.chat(Component.translatable("chat.vwp.porter.worker_needs_items", targetWorker.getName(), needs));

        List<ItemStack> itemsNeeded = determineItemsNeeded(targetWorker);
        task.setItemsToDeliver(itemsNeeded);

        BlockPos workerChestPos = findWorkerChest(targetWorker);
        task.setTargetWorkerChestPos(workerChestPos);

        BlockPos porterChestPos = findPorterChest();
        if (porterChestPos == null) {
            porter.chat(Component.translatable("chat.vwp.porter.no_chest"));
            task.reset();
            return;
        }

        task.setOwnChestPos(porterChestPos);
        task.transitionTo(DeliveryTask.State.GOING_TO_OWN_CHEST);
        assessmentStartTime = 0;
    }
    
    private AbstractWorkerEntity findTargetWorker() {
        DeliveryTask task = porter.getDeliveryTask();
        if (task.getTargetWorker() == null) {
            return null;
        }
        
        List<AbstractWorkerEntity> nearbyWorkers = porter.level().getEntitiesOfClass(
            AbstractWorkerEntity.class,
            porter.getBoundingBox().inflate(CommonConfig.porterScanRadius),
            worker -> worker.getUUID().equals(task.getTargetWorker().getId())
        );
        
        return nearbyWorkers.isEmpty() ? null : nearbyWorkers.get(0);
    }
    
    private boolean checkWorkerNeeds(AbstractWorkerEntity worker) {
        if (worker.needsMainTool || worker.needsSecondTool) {
            return true;
        }
        
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
    
    private String describeNeeds(AbstractWorkerEntity worker) {
        List<String> needs = new ArrayList<>();
        
        if (worker.needsMainTool || worker.needsSecondTool) {
            needs.add("tools");
        }
        
        if (worker.needsToEat()) {
            needs.add("food");
            return String.join(" and ", needs);
        }
        
        int totalFood = countFoodInInventory(worker);
        if (totalFood >= 3) {
            return String.join(" and ", needs);
        }
        
        BlockPos workerChestPos = findWorkerChest(worker);
        if (workerChestPos == null) {
            needs.add("food");
            return String.join(" and ", needs);
        }
        
        Container workerChest = getContainer(workerChestPos);
        if (workerChest == null) {
            needs.add("food");
            return String.join(" and ", needs);
        }
        
        totalFood += countFoodInContainer(workerChest);
        if (totalFood < 3) {
            needs.add("food");
        }
        
        return String.join(" and ", needs);
    }
    
    private List<ItemStack> determineItemsNeeded(AbstractWorkerEntity worker) {
        List<ItemStack> items = new ArrayList<>();
        
        if (worker.needsMainTool || worker.needsSecondTool) {
            // Tool logic would be added here based on worker type
        }
        
        int foodNeeded = 3 - countFoodInInventory(worker);
        BlockPos workerChestPos = findWorkerChest(worker);
        if (workerChestPos != null) {
            Container workerChest = getContainer(workerChestPos);
            if (workerChest != null) {
                foodNeeded -= countFoodInContainer(workerChest);
            }
        }
        
        // Food items would be determined here
        
        return items;
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
    
    private BlockPos findPorterChest() {
        BlockPos porterChest = porter.getChestPos();
        if (porterChest != null) {
            return porterChest;
        }
        
        BlockPos porterPos = porter.getStartPos();
        if (porterPos == null) {
            porterPos = porter.blockPosition();
        }
        
        for (BlockPos pos : BlockPos.betweenClosed(
            porterPos.offset(-3, -2, -3),
            porterPos.offset(3, 2, 3))) {
            BlockEntity entity = porter.level().getBlockEntity(pos);
            if (entity instanceof ChestBlockEntity || entity instanceof Container) {
                return pos;
            }
        }
        return null;
    }
}