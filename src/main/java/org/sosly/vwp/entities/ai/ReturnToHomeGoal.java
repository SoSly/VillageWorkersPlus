package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.utils.Worker;

import java.util.EnumSet;

public class ReturnToHomeGoal extends Goal {
    AbstractWorkerEntity worker;
    long lastWentHome = 0;

    public ReturnToHomeGoal(AbstractWorkerEntity worker) {
        this.worker = worker;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        if (worker.getStartPos() == null) {
            return false;
        }

        if (worker.level().getGameTime() - lastWentHome < 100) { // 5 seconds cooldown
            return false;
        }

        double distance = worker.distanceToSqr(worker.getStartPos().getX(), worker.getStartPos().getY(), worker.getStartPos().getZ());
        return distance > 30.0; // 15 blocks away
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        BlockPos pos = worker.getStartPos();
        worker.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0);
    }

    @Override
    public void stop() {
        lastWentHome = worker.level().getGameTime();
    }
}
