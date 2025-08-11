package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.data.ItemPredicate;
import org.sosly.vwp.tasks.AbstractTask;
import org.sosly.vwp.utils.Containers;
import org.sosly.vwp.utils.Worker;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class GetItemsFromContainerGoal extends AbstractTaskGoal {
    private AbstractWorkerEntity worker;
    private BlockPos pos;
    private List<ItemPredicate> items;
    private int lastTicked = 0;
    private int currentNeedIndex = 0;
    private boolean[] attemptedNeeds;

    public GetItemsFromContainerGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task) {
        super(task);
        this.worker = worker;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    public GetItemsFromContainerGoal(AbstractWorkerEntity worker, List<ItemPredicate> items, BlockPos pos) {
        super(null);
        this.worker = worker;
        this.items = items;
        this.pos = pos;
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        if (task != null && !canUseTask()) {
            return false;
        }

        if (pos == null || items == null) {
            return false;
        }

        double distance = worker.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
        return distance < 2.5D; // TODO: consider using a configuration option for the distance threshold
    }

    @Override
    public boolean canContinueToUse() {
        if (allNeedsAttempted()) {
            return false;
        }

        return this.canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        currentNeedIndex = 0;
        attemptedNeeds = new boolean[items.size()];

        if (!(worker.getOwner() instanceof Player player)) {
            return;
        }

        BlockEntity be = worker.level().getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            chest.startOpen(player);
        }

    }

    @Override
    public void stop() {
        if (!(worker.getOwner() instanceof Player player)) {
            return;
        }

        BlockEntity be = worker.level().getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            chest.stopOpen(player);
        }

        currentNeedIndex = 0;
        attemptedNeeds = null;
        items = null;
        pos = null;

        if (getTask() != null) {
            getTask().next();
        }
    }

    @Override
    public void tick() {
        if (worker.level().getGameTime() - lastTicked < 20) {
            return;
        }
        lastTicked = (int) worker.level().getGameTime();

        if (!Worker.hasEmptyInventorySlot(worker)) {
            return;
        }

        Container chest = Containers.getFromBlockPos(pos, worker.level()).orElse(null);
        if (chest == null) {
            return;
        }

        if (currentNeedIndex >= items.size()) {
            return;
        }

        if (attemptedNeeds[currentNeedIndex]) {
            currentNeedIndex++;
            return;
        }

        ItemPredicate need = items.get(currentNeedIndex);

        int currentAmount = countItemsInInventory(worker.getInventory(), need);
        int desiredAmount = need.getAmount();
        if (currentAmount >= desiredAmount) {
            attemptedNeeds[currentNeedIndex] = true;
            currentNeedIndex++;
            return;
        }

        int amountToGet = Math.min(desiredAmount - currentAmount, desiredAmount);
        ItemStack extracted = Containers.removeItem(chest, need, amountToGet);

        if (extracted.isEmpty()) {
            attemptedNeeds[currentNeedIndex] = true;
            currentNeedIndex++;
            return;
        }

        ItemStack remainder = worker.getInventory().addItem(extracted);
        if (!remainder.isEmpty()) {
            remainder = Containers.putItem(chest, remainder);

            if (!remainder.isEmpty()) {
                VillageWorkersPlus.LOGGER.error("Failed to put back remainder: {}", remainder);
            }
        }
    }

    private boolean canUseTask() {
        if (!getTask().isCurrentStep(this)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            getTask().reset();
            return false;
        }

        if (pos == null) {
            pos = getTask().currentData(BlockPos.class);
        }

        if (items == null && getTaskItems().isPresent()) {
            items = getTaskItems().get();
        }

        return true;
    }

    private int countItemsInInventory(Container inventory, ItemPredicate predicate) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (predicate.matches(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private boolean allNeedsAttempted() {
        if (attemptedNeeds == null) return true;
        for (boolean attempted : attemptedNeeds) {
            if (!attempted) return false;
        }
        return true;
    }
}
