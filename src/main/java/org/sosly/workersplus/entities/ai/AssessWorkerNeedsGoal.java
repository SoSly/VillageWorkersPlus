package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.config.CommonConfig;
import org.sosly.workersplus.data.Need;
import org.sosly.workersplus.tasks.AbstractTask;
import org.sosly.workersplus.utils.Chat;
import org.sosly.workersplus.utils.Worker;

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
                .filter(abstractWorkerEntity -> worker.position()
                        .closerThan(abstractWorkerEntity.position(), CommonConfig.workerAssessmentDistance))
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
        Chat.send(worker, Component.translatable("chat.workersplus.assess_worker_needs.success",
                target.getName()));
        getTask().next();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
