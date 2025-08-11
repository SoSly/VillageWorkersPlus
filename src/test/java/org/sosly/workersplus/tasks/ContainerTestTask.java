package org.sosly.workersplus.tasks;

import net.minecraft.core.BlockPos;
import org.sosly.workersplus.entities.ai.GetItemsFromContainerGoal;

import java.util.Optional;

public class ContainerTestTask extends AbstractTask<ContainerTestTask.State> {
    private final BlockPos pos;

    public ContainerTestTask(BlockPos pos) {
        super(State.class);
        this.pos = pos;
    }

    @Override
    public void setData(String key, Object value) {}

    @Override
    public Optional<Object> getData(String key) {
        return Optional.empty();
    }

    public enum State implements TaskState {
        WORKING(new Step(GetItemsFromContainerGoal.class,
            t -> ((ContainerTestTask) t).pos)),

        ;

        private final Step step;

        State(Step step) {
            this.step = step;
        }

        public Step getStep() {
            return step;
        }
    }
}
