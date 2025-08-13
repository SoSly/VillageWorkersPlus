package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import org.sosly.workersplus.tasks.AbstractTask;

public class StartTaskGoal extends AbstractTaskGoal {
    private final AbstractWorkerEntity worker;
    private long lastAttemptTick = 0;
    
    public StartTaskGoal(AbstractWorkerEntity worker, AbstractTask<?> task) {
        super(task);
        this.worker = worker;
    }

    @Override
    public boolean canUse() {
        if (getTask().current() != null) {
            return false;
        }
        
        long currentTick = worker.level().getGameTime();
        if (currentTick - lastAttemptTick < 20) {
            return false;
        }
        
        return getTask().canStart();
    }

    @Override
    public void start() {
        lastAttemptTick = worker.level().getGameTime();
        getTask().start();
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
