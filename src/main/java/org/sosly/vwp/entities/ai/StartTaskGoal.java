package org.sosly.vwp.entities.ai;

import org.sosly.vwp.tasks.AbstractTask;

import java.util.function.Supplier;

public class StartTaskGoal extends AbstractTaskGoal {
    public StartTaskGoal(Supplier<AbstractTask<?>> task) {
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
