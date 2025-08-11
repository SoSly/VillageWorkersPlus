package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.data.ItemPredicate;
import org.sosly.vwp.tasks.AbstractTask;
import org.sosly.vwp.utils.Chat;
import org.sosly.vwp.utils.Containers;
import org.sosly.vwp.utils.Worker;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

public class PutItemsInContainerGoal extends AbstractTaskGoal {
    private AbstractWorkerEntity worker;
    private BlockPos pos;
    private List<ItemPredicate> items;
    private int lastTicked = 0;
    private int currentNeedIndex = 0;
    private boolean[] attemptedNeeds;
    public PutItemsInContainerGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task) {
        super(task);
        this.worker = worker;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    public PutItemsInContainerGoal(AbstractWorkerEntity worker, List<ItemPredicate> items, BlockPos pos) {
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

        return pos.closerToCenterThan(worker.position(), CommonConfig.containerReachDistance);
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
        if (task != null) {
            getTask().next();
        }

        BlockEntity be = worker.level().getBlockEntity(pos);
        currentNeedIndex = 0;
        attemptedNeeds = null;
        items = null;
        pos = null;

        if (!(worker.getOwner() instanceof Player player)) {
            return;
        }

        if (be instanceof ChestBlockEntity chest) {
            chest.stopOpen(player);
        }
    }

    @Override
    public void tick() {
        if (worker.level().getGameTime() - lastTicked < 20) {
            return;
        }
        lastTicked = (int) worker.level().getGameTime();

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
        
        // Remove matching items from worker's inventory
        ItemStack toDeposit = Containers.removeItem(worker.getInventory(), need, need.getAmount());
        
        if (toDeposit.isEmpty()) {
            attemptedNeeds[currentNeedIndex] = true;
            currentNeedIndex++;
            return;
        }

        ItemStack depositing = toDeposit.copy();
        ItemStack remainder = Containers.putItem(chest, toDeposit);

        // Calculate how many were actually placed before modifying remainder
        int placed = depositing.getCount() - remainder.getCount();
        
        if (!remainder.isEmpty()) {
            remainder = worker.getInventory().addItem(remainder);
            if (!remainder.isEmpty()) {
                VillageWorkersPlus.LOGGER.error("Failed to put back remainder in worker inventory: {}", remainder);
            }
        }

        if (placed > 0) {
            Chat.send(worker, Component.translatable("chat.vwp.put_items_in_container.success",
                    placed, depositing.getDisplayName()));
        }
    }

    private boolean canUseTask() {
        if (!getTask().isCurrentStep(this)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            VillageWorkersPlus.LOGGER.warn("Worker {} is stuck in PutItemsInContainerGoal, resetting task.",
                    worker.getUUID());
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

    private boolean allNeedsAttempted() {
        if (attemptedNeeds == null) return true;
        for (boolean attempted : attemptedNeeds) {
            if (!attempted) return false;
        }
        return true;
    }
}
