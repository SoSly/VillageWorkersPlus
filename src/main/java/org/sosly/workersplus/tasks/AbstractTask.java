package org.sosly.workersplus.tasks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.workersplus.VillageWorkersPlus;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractTask<S extends Enum<S> & TaskState> {
    private final S[] states;
    protected int step = -1;
    protected long stateStartTime;
    protected long taskStartTime;
    private final TaskCoordinator coordinator;

    private final String CURRENT_STEP = "CurrentStep";
    private final String STATE_START_TIME = "StateStartTime";
    private final String TASK_START_TIME = "TaskStartTime";

    public AbstractTask(Class<S> stateClass, TaskCoordinator coordinator) {
        this.states = stateClass.getEnumConstants();
        this.coordinator = coordinator;
    }

    // ------------------------
    // Step navigation
    // ------------------------
    public void next() {
        step++;
        stateStartTime = System.currentTimeMillis();
        
        if (complete()) {
            reset();
        }
    }

    public void previous() {
        step--;
        stateStartTime = System.currentTimeMillis();
    }

    @Nullable
    public TaskState.Step current() {
        if (step < 0 || step >= states.length) {
            return null;
        }
        return states[step].getStep();
    }

    public boolean isCurrentStep(Goal goal) {
        if (step < 0 || step >= states.length) {
            return false;
        }
        return Objects.requireNonNull(current()).isGoal(goal);
    }

    public boolean complete() {
        return step >= states.length;
    }

    public <T> T currentData(Class<T> type) {
        return Objects.requireNonNull(current()).getData(this, type);
    }

    public boolean canStart() {
        return coordinator == null || coordinator.canStartTask(this);
    }
    
    public void start() {
        if (!canStart()) {
            VillageWorkersPlus.LOGGER.debug("Task {} blocked from starting - another task is active: {}", 
                this.getClass().getSimpleName(), 
                coordinator.getActiveTask() != null ? coordinator.getActiveTask().getClass().getSimpleName() : "null");
            return;
        }
        
        VillageWorkersPlus.LOGGER.debug("Starting task: {}", this.getClass().getSimpleName());
        
        if (coordinator != null) {
            coordinator.setActiveTask(this);
        }
        
        taskStartTime = System.currentTimeMillis();
        stateStartTime = taskStartTime;
        step = 0;
    }
    
    public boolean isActive() {
        return step >= 0 && step < states.length;
    }

    // ------------------------
    // Timing
    // ------------------------
    public long getStateStartTime() {
        return stateStartTime;
    }

    public long getTimeInState() {
        long currentTime = System.currentTimeMillis();
        return (stateStartTime > 0) ? currentTime - stateStartTime : 0;
    }

    public long getTotalTime() {
        long currentTime = System.currentTimeMillis();
        return (taskStartTime > 0) ? currentTime - taskStartTime : 0;
    }

    // ------------------------
    // Persistence
    // ------------------------
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CURRENT_STEP, step);
        tag.putLong(STATE_START_TIME, stateStartTime);
        tag.putLong(TASK_START_TIME, taskStartTime);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains(CURRENT_STEP)) {
            step = tag.getInt(CURRENT_STEP);
        } else {
            step = -1;
        }
        stateStartTime = tag.getLong(STATE_START_TIME);
        taskStartTime = tag.getLong(TASK_START_TIME);
    }

    public void reset() {
        if (coordinator != null) {
            coordinator.clearActiveTask(this);
        }

        VillageWorkersPlus.LOGGER.debug("Resetting task: {}", this.getClass().getSimpleName());
        step = -1;
        stateStartTime = 0;
        taskStartTime = 0;
    };


    // ------------------------
    // Hooks for subclasses
    // ------------------------
    public abstract void setData(String key, Object value);
    public abstract Optional<Object> getData(String key);
}
