package org.sosly.workersplus.entities.ai;

import org.sosly.workersplus.tasks.AbstractTask;

public class StartTaskGoal extends AbstractTaskGoal {
    public StartTaskGoal(AbstractTask<?> task) {
        super(task);
    }

    @Override
    public boolean canUse() {
        return getTask().current() == null;
    }

    @Override
    public void start() {
        getTask().start();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
