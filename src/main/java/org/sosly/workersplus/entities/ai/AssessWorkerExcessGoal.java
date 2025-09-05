package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.config.CommonConfig;
import org.sosly.workersplus.data.Want;
import org.sosly.workersplus.tasks.AbstractTask;
import org.sosly.workersplus.utils.Chat;
import org.sosly.workersplus.utils.Containers;
import org.sosly.workersplus.utils.Worker;

import java.util.*;

public class AssessWorkerExcessGoal extends AbstractTaskGoal {
    private final AbstractWorkerEntity collector;

    public AssessWorkerExcessGoal(AbstractWorkerEntity collector, AbstractTask<?> task) {
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
            VillageWorkersPlus.LOGGER.warn("Worker {} is stuck in AssessWorkerExcessGoal, resetting task.",
                    collector.getUUID());
            getTask().reset();
            return false;
        }

        return getTaskTarget()
                .filter(target -> {
                    BlockPos chestPos = target.getChestPos();
                    return chestPos != null && collector.position()
                            .closerThan(chestPos.getCenter(), CommonConfig.containerReachDistance);
                })
                .isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canUse()) {
            return false;
        }

        return getTask().getTimeInState() < 3000;
    }

    @Override
    public void stop() {
        if (!getTask().isCurrentStep(this)) {
            return;
        }

        Optional<AbstractWorkerEntity> taskTarget = getTaskTarget();
        if (taskTarget.isEmpty()) {
            getTask().reset();
            return;
        }

        AbstractWorkerEntity target = taskTarget.get();
        BlockPos chestPos = target.getChestPos();
        if (chestPos == null) {
            getTask().reset();
            return;
        }

        Container chest = Containers.getFromBlockPos(chestPos, collector.level()).orElse(null);
        if (chest == null) {
            getTask().reset();
            return;
        }

        List<Want> itemsToCollect = identifyExcessItems(target, chest);
        if (itemsToCollect.isEmpty()) {
            getTask().reset();
            return;
        }

        getTask().setData("items", itemsToCollect);
        Chat.send(collector, Component.translatable("chat.workersplus.assess_worker_excess.success",
                target.getName(), itemsToCollect.size()));
        getTask().next();
    }

    private List<Want> identifyExcessItems(AbstractWorkerEntity target, Container chest) {
        List<Want> itemsToCollect = new ArrayList<>();
        Map<String, Integer> wantedItemCounts = new HashMap<>();

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i).copy();
            if (stack.isEmpty()) {
                continue;
            }

            boolean wantsIt = target.wantsToKeep(stack);
            VillageWorkersPlus.LOGGER.info("Slot {}: {} x{}, worker wants: {}", 
                i, stack.getItem(), stack.getCount(), wantsIt);

            if (!wantsIt) {
                itemsToCollect.add(new Want(stack));
                VillageWorkersPlus.LOGGER.info("  -> Marked as excess (unwanted)");
            } else if (stack.isStackable()) {
                String itemKey = stack.getItem().toString();
                int currentCount = wantedItemCounts.getOrDefault(itemKey, 0);
                int totalCount = currentCount + stack.getCount();
                
                if (currentCount >= stack.getMaxStackSize()) {
                    itemsToCollect.add(new Want(stack));
                } else if (totalCount > stack.getMaxStackSize()) {
                    int excess = totalCount - stack.getMaxStackSize();
                    ItemStack excessStack = stack.copy();
                    excessStack.setCount(excess);
                    itemsToCollect.add(new Want(excessStack));
                    wantedItemCounts.put(itemKey, stack.getMaxStackSize());
                } else {
                    wantedItemCounts.put(itemKey, totalCount);
                }
            }
        }

        VillageWorkersPlus.LOGGER.info("Total excess items identified: {}", itemsToCollect.size());
        return itemsToCollect;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}