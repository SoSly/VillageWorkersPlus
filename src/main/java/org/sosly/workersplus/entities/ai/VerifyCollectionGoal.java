package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.data.ItemPredicate;
import org.sosly.workersplus.data.Want;
import org.sosly.workersplus.tasks.AbstractTask;
import org.sosly.workersplus.utils.Worker;

import java.util.*;

public class VerifyCollectionGoal extends AbstractTaskGoal {
    private final AbstractWorkerEntity collector;
    private static final int MAX_RETRIES = 3;
    private int retryCount = 0;

    public VerifyCollectionGoal(AbstractWorkerEntity collector, AbstractTask<?> task) {
        super(task);
        this.collector = collector;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(collector) || !getTask().isCurrentStep(this)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            VillageWorkersPlus.LOGGER.warn("Worker {} is stuck in VerifyCollectionGoal, resetting task.",
                    collector.getUUID());
            getTask().reset();
            return false;
        }

        return getTaskItems().isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        Optional<List<ItemPredicate>> taskItemsOpt = getTaskItems();
        if (taskItemsOpt.isEmpty()) {
            getTask().reset();
            return;
        }

        List<?> taskItems = taskItemsOpt.get();
        if (taskItems.isEmpty()) {
            getTask().next();
            return;
        }

        List<Want> originalWants = extractWants(taskItems);
        if (originalWants.isEmpty()) {
            getTask().next();
            return;
        }

        List<Want> collectedItems = new ArrayList<>();
        List<Want> missingItems = new ArrayList<>();
        
        categorizeItems(originalWants, collectedItems, missingItems);

        if (collectedItems.isEmpty()) {
            VillageWorkersPlus.LOGGER.warn("Collection failed - no items collected");
            getTask().reset();
            return;
        }

        boolean hasSpace = Worker.hasEmptyInventorySlot(collector);
        boolean hasMissingItems = !missingItems.isEmpty();
        
        if (hasMissingItems && hasSpace && retryCount < MAX_RETRIES) {
            retryCount++;
            getTask().setData("items", missingItems);
            VillageWorkersPlus.LOGGER.debug("Retrying collection - {} items missing, attempt {}/{}", 
                    missingItems.size(), retryCount, MAX_RETRIES);
            getTask().previous();
            return;
        }

        retryCount = 0;
        getTask().setData("items", collectedItems);
        
        if (hasMissingItems) {
            VillageWorkersPlus.LOGGER.debug("Partial collection - got {} of {} items", 
                    collectedItems.size(), originalWants.size());
        }
        
        getTask().next();
    }

    private List<Want> extractWants(List<?> taskItems) {
        List<Want> wants = new ArrayList<>();
        for (Object item : taskItems) {
            if (item instanceof Want want) {
                wants.add(want);
            }
        }
        return wants;
    }

    private void categorizeItems(List<Want> originalWants, List<Want> collectedItems, List<Want> missingItems) {
        for (Want want : originalWants) {
            int collectedAmount = countInInventory(want);
            
            if (collectedAmount > 0) {
                ItemStack representativeStack = findRepresentativeStack(want);
                if (representativeStack != null) {
                    ItemStack collectedStack = representativeStack.copy();
                    collectedStack.setCount(Math.min(collectedAmount, want.getAmount()));
                    collectedItems.add(new Want(collectedStack));
                }
            }
            
            if (collectedAmount < want.getAmount()) {
                int remaining = want.getAmount() - collectedAmount;
                missingItems.add(new Want(want::matches, remaining, want.getName()));
            }
        }
    }

    private int countInInventory(Want want) {
        int count = 0;
        for (int i = 0; i < collector.getInventory().getContainerSize(); i++) {
            ItemStack stack = collector.getInventory().getItem(i);
            if (!stack.isEmpty() && want.matches(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private ItemStack findRepresentativeStack(Want want) {
        for (int i = 0; i < collector.getInventory().getContainerSize(); i++) {
            ItemStack stack = collector.getInventory().getItem(i);
            if (!stack.isEmpty() && want.matches(stack)) {
                return stack;
            }
        }
        return null;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}