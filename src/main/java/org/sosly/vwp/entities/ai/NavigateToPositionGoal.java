package org.sosly.vwp.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.function.Supplier;

public class NavigateToPositionGoal extends Goal {
    private final PathfinderMob mob;
    private final Supplier<BlockPos> targetPosSupplier;
    private final double closeEnoughDistance;
    private final Supplier<Boolean> shouldContinue;
    private BlockPos lastTargetPos;
    
    public NavigateToPositionGoal(PathfinderMob mob, Supplier<BlockPos> targetPosSupplier, 
                                  double closeEnoughDistance, 
                                  Supplier<Boolean> shouldContinue) {
        this.mob = mob;
        this.targetPosSupplier = targetPosSupplier;
        this.closeEnoughDistance = closeEnoughDistance;
        this.shouldContinue = shouldContinue;
    }
    
    @Override
    public boolean canUse() {
        if (!shouldContinue.get()) {
            return false;
        }
        
        BlockPos targetPos = targetPosSupplier.get();
        if (targetPos == null) {
            return false;
        }
        
        double distanceSq = mob.distanceToSqr(
            targetPos.getX() + 0.5, 
            targetPos.getY(), 
            targetPos.getZ() + 0.5
        );
        
        return distanceSq > closeEnoughDistance * closeEnoughDistance;
    }
    
    @Override
    public boolean canContinueToUse() {
        if (!shouldContinue.get()) {
            return false;
        }
        
        BlockPos targetPos = targetPosSupplier.get();
        if (targetPos == null) {
            return false;
        }
        
        double distanceSq = mob.distanceToSqr(
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5
        );
        
        if (distanceSq <= closeEnoughDistance * closeEnoughDistance) {
            return false;
        }
        
        return !mob.getNavigation().isDone() || shouldRepath(targetPos);
    }
    
    @Override
    public void start() {
        BlockPos targetPos = targetPosSupplier.get();
        if (targetPos == null) {
            return;
        }
        
        lastTargetPos = targetPos;
        mob.getNavigation().moveTo(
            targetPos.getX() + 0.5,
            targetPos.getY(),
            targetPos.getZ() + 0.5,
            1.0
        );
    }
    
    @Override
    public void tick() {
        BlockPos targetPos = targetPosSupplier.get();
        if (targetPos == null) {
            return;
        }
        
        if (shouldRepath(targetPos)) {
            lastTargetPos = targetPos;
            mob.getNavigation().moveTo(
                targetPos.getX() + 0.5,
                targetPos.getY(),
                targetPos.getZ() + 0.5,
                1.0
            );
        }
    }
    
    @Override
    public void stop() {
        mob.getNavigation().stop();
        lastTargetPos = null;
        
        if (mob instanceof Porter porter) {
            DeliveryTask task = porter.getDeliveryTask();
            if (task.isInState(DeliveryTask.State.NAVIGATING_TO_WORKER)) {
                task.transitionTo(DeliveryTask.State.ASSESSING_NEEDS);
                task.markStateStart(porter.level().getGameTime());
            }
        }
    }
    
    private boolean shouldRepath(BlockPos targetPos) {
        if (lastTargetPos == null || !lastTargetPos.equals(targetPos)) {
            return true;
        }
        
        return mob.getNavigation().isDone();
    }
}