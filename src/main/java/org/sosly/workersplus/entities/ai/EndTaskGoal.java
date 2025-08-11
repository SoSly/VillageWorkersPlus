package org.sosly.workersplus.entities.ai;

import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.tasks.AbstractTask;

import java.util.function.Supplier;

public class EndTaskGoal extends AbstractTaskGoal {
    public EndTaskGoal(Supplier<AbstractTask<?>> task) {
        super(task);
    }

    @Override
    public boolean canUse() {
        return getTask().complete();
    }

    @Override
    public void start() {
        getTask().reset();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
