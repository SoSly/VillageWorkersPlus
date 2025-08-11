package org.sosly.workersplus.entities.ai;

import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.tasks.AbstractTask;

public class EndTaskGoal extends AbstractTaskGoal {
    public EndTaskGoal(AbstractTask<?> task) {
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
