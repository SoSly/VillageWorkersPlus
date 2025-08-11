package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.workersplus.data.ItemPredicate;
import org.sosly.workersplus.tasks.AbstractTask;

import java.util.List;
import java.util.Optional;
abstract class AbstractTaskGoal extends Goal {
    protected final AbstractTask<?> task;

    protected final long stepTimeout = 10 * 1000; // 10 seconds, todo: use a configuration option for the timeout
    protected final long taskTimeout = 60 * 1000; // 60 seconds, todo: use a configuration option for the timeout

    public AbstractTaskGoal(AbstractTask<?> task) {
        this.task = task;
    }

    protected AbstractTask<?> getTask() {
        return task;
    }

    public Optional<List<ItemPredicate>> getTaskItems() {
        if (task == null) {
            return Optional.empty();
        }

        return task.getData("items")
                .filter(List.class::isInstance)
                .map(list -> (List<ItemPredicate>) list);
    }

    public Optional<AbstractWorkerEntity> getTaskTarget() {
        if (task == null) {
            return Optional.empty();
        }

        return task.getData("target")
                .filter(AbstractWorkerEntity.class::isInstance)
                .map(AbstractWorkerEntity.class::cast);
    }

    protected boolean isStuckInCurrentStep() {
        return task.isCurrentStep(this) && task.getStateStartTime() > 0 &&
                (task.getTimeInState() > stepTimeout || task.getTotalTime() > taskTimeout);
    }
}
