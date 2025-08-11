package org.sosly.workersplus.tasks;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Function;
import java.util.function.Supplier;

public interface TaskState {
    Step getStep();

    class Step {
        private final Class<? extends Goal> goal;
        private final Function<AbstractTask<?>, ?> supplier;
        private Object data;

        public Step() {
            this.goal = null;
            this.supplier = null;
            this.data = null;
        }

        public Step(Class<? extends Goal> goal) {
            this.goal = goal;
            this.supplier = null;
            this.data = null;
        }

        public Step(Class<? extends Goal> goal, Function<AbstractTask<?>, ?> supplier) {
            this.goal = goal;
            this.supplier = supplier;
        }

        public boolean isGoal(Goal goal) {
            return this.goal.isInstance(goal);
        }

        public void setData(Object data) {
            this.data = data;
        }

        public <T> T getData(AbstractTask<?> task, Class<T> type) {
            if (data != null) {
                return type.cast(data);
            }

            if (supplier == null) {
                return null;
            }

            Object result = supplier.apply(task);
            if (result == null) {
                return null;
            }

            if (!type.isInstance(result)) {
                throw new IllegalStateException("Expected data of type " + type.getName() + ", but got " + result.getClass().getName());
            }

            return type.cast(result);
        }
    }
}
