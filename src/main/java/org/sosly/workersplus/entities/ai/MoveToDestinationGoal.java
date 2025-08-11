package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.tasks.AbstractTask;
import org.sosly.workersplus.utils.Worker;

import java.util.EnumSet;
import java.util.function.Supplier;

public class MoveToDestinationGoal extends AbstractTaskGoal {
    private final AbstractWorkerEntity worker;
    private final double distanceThreshold;
    private BlockPos destination;


    public MoveToDestinationGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task, double distanceThreshold) {
        super(task);
        this.worker = worker;
        this.distanceThreshold = distanceThreshold;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public MoveToDestinationGoal(AbstractWorkerEntity worker, double distanceThreshold, BlockPos destination) {
        super(null);
        this.worker = worker;
        this.destination = destination;
        this.distanceThreshold = distanceThreshold;
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        if (task != null && !canUseTask()) {
            return false;
        }

        if (destination == null) {
            return false;
        }

        double distance = worker.distanceToSqr(destination.getX(), destination.getY(), destination.getZ());
        return distance > distanceThreshold;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.canUse()) {
            return false;
        }

        double distance = worker.distanceToSqr(destination.getX(), destination.getY(), destination.getZ());
        return distance > distanceThreshold;
    }

    @Override
    public void tick() {
        worker.getNavigation()
                .moveTo(destination.getX(), destination.getY(), destination.getZ(), 1.0);
    }

    @Override
    public void stop() {
        if (task != null) {
            getTask().next();
        }

        destination = null;
        worker.getNavigation().stop();
    }

    private boolean canUseTask() {
        if (!getTask().isCurrentStep(this)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            getTask().reset();
            return false;
        }

        BlockPos destination = getTask().currentData(BlockPos.class);
        if (destination == null) {
            return false;
        }

        this.destination = destination;
        return true;
    }
}
