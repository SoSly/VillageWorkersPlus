package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.data.Need;
import org.sosly.vwp.tasks.AbstractTask;
import org.sosly.vwp.utils.Chat;
import org.sosly.vwp.utils.Worker;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class AssessWorkerNeedsGoal extends AbstractTaskGoal {
    private final AbstractWorkerEntity worker;

    public AssessWorkerNeedsGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task) {
        super(task);
        this.worker = worker;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker) || !getTask().isCurrentStep(this)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            VillageWorkersPlus.LOGGER.warn("Worker {} is stuck in AssessWorkerNeedsGoal, resetting task.",
                    worker.getUUID());
            getTask().reset();
            return false;
        }

        return getTaskTarget()
                .filter(abstractWorkerEntity -> worker.blockPosition()
                        .closerThan(abstractWorkerEntity.blockPosition(), 5)) // todo: use a configuration option for the distance
                .isPresent();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canUse()) {
            return false;
        }

        // todo: use a configuration option for the timeout
        return getTask().getTimeInState() > 3000;
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
        List<Need> needs = Worker.getNeeds(target);
        if (needs.isEmpty()) {
            getTask().reset();
            return;
        }

        getTask().setData("items", needs);
        Chat.send(worker, Component.translatable("chat.vwp.assess_worker_needs.success",
                target.getName()));
        getTask().next();
        VillageWorkersPlus.LOGGER.debug("Worker {} assessed needs of {}: {}",
                worker.getUUID(), target.getUUID(), needs);
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
