package org.sosly.vwp.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.function.Supplier;

public class ReturnHomeGoal extends Goal {
    private final PathfinderMob mob;
    private final Supplier<BlockPos> homePosSupplier;
    private final Supplier<Boolean> shouldReturn;
    private final Runnable onReturned;
    
    public ReturnHomeGoal(PathfinderMob mob, Supplier<BlockPos> homePosSupplier,
                         Supplier<Boolean> shouldReturn, Runnable onReturned) {
        this.mob = mob;
        this.homePosSupplier = homePosSupplier;
        this.shouldReturn = shouldReturn;
        this.onReturned = onReturned;
    }
    
    @Override
    public boolean canUse() {
        if (!shouldReturn.get()) {
            return false;
        }
        
        BlockPos homePos = homePosSupplier.get();
        if (homePos == null) {
            return false;
        }
        
        double distanceSq = mob.distanceToSqr(
            homePos.getX() + 0.5,
            homePos.getY(),
            homePos.getZ() + 0.5
        );
        
        return distanceSq > 9.0;
    }
    
    @Override
    public boolean canContinueToUse() {
        if (!shouldReturn.get()) {
            return false;
        }
        
        BlockPos homePos = homePosSupplier.get();
        if (homePos == null) {
            return false;
        }
        
        double distanceSq = mob.distanceToSqr(
            homePos.getX() + 0.5,
            homePos.getY(),
            homePos.getZ() + 0.5
        );
        
        if (distanceSq <= 9.0) {
            return false;
        }
        
        return !mob.getNavigation().isDone();
    }
    
    @Override
    public void start() {
        BlockPos homePos = homePosSupplier.get();
        if (homePos == null) {
            return;
        }
        
        mob.getNavigation().moveTo(
            homePos.getX() + 0.5,
            homePos.getY(),
            homePos.getZ() + 0.5,
            1.0
        );
    }
    
    @Override
    public void tick() {
        BlockPos homePos = homePosSupplier.get();
        if (homePos == null) {
            return;
        }
        
        if (mob.getNavigation().isDone()) {
            mob.getNavigation().moveTo(
                homePos.getX() + 0.5,
                homePos.getY(),
                homePos.getZ() + 0.5,
                1.0
            );
        }
    }
    
    @Override
    public void stop() {
        mob.getNavigation().stop();
        
        if (onReturned != null) {
            onReturned.run();
        }
        
        if (mob instanceof Porter porter) {
            DeliveryTask task = porter.getDeliveryTask();
            if (task != null) {
                task.reset();
            }
        }
    }
}