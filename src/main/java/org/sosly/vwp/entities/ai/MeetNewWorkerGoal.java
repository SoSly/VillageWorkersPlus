package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.data.WorkerInfo;
import org.sosly.vwp.data.WorkerRelationships;
import org.sosly.vwp.utils.Chat;
import org.sosly.vwp.utils.Worker;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MeetNewWorkerGoal extends Goal {
    private final Supplier<WorkerRelationships> relationships;
    private final AbstractWorkerEntity worker;
    private long introductionStartTime = 0;
    private long lastScanTime = 0;
    private boolean isIntroducing;
    private boolean isMovingToTarget;

    public MeetNewWorkerGoal(AbstractWorkerEntity worker, Supplier<WorkerRelationships> relationships) {
        this.worker = worker;
        this.relationships = relationships;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        long currentTime = worker.level().getGameTime();

        // todo: Move this to a config option
        if (currentTime - lastScanTime < 100L) {
            return false;
        }

        lastScanTime = currentTime;
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        if (worker.getTarget() == null || !worker.getTarget().isAlive()) {
            return false;
        }

        if (!isIntroducing && !isMovingToTarget) {
            return false;
        }

        if (isMovingToTarget) {
            return true;
        }

        long currentTime = worker.level().getGameTime();
        return currentTime - introductionStartTime <= CommonConfig.workerMeetingDuration;
    }

    @Override
    public void start() {
        AbstractWorkerEntity targetWorker = findUnknownWorker();

        if (targetWorker == null) {
            return;
        }

        isMovingToTarget = true;
        worker.setTarget(targetWorker);
        Chat.send(worker, Component.translatable("chat.vwp.meet_new_worker.start"));
    }

    @Override
    public void tick() {
        if (!isIntroducing && !isMovingToTarget) {
            return;
        }

        LivingEntity target = worker.getTarget();
        if (!(target instanceof AbstractWorkerEntity)) {
            return;
        }

        if (!worker.position().closerThan(target.position(), CommonConfig.workerChatRadius)) {
            worker.getNavigation().moveTo(target, 1.0);
            return;
        }

        if (!isIntroducing) {
            isIntroducing = true;
            isMovingToTarget = false;
            introductionStartTime = worker.level().getGameTime();
        }
    }

    @Override
    public void stop() {
        if (!isIntroducing) {
            isMovingToTarget = false;
            return;
        }

        LivingEntity target = worker.getTarget();
        if (target == null || !(target instanceof AbstractWorkerEntity targetWorker)) {
            return;
        }

        UUID targetUUID = targetWorker.getUUID();
        Component name = targetWorker.getName();
        BlockPos targetPos = targetWorker.blockPosition();
        WorkerInfo workerInfo = new WorkerInfo(targetUUID, name, targetPos);
        relationships.get().addKnown(targetUUID, workerInfo);

        Chat.send(worker, Component.translatable("chat.vwp.meet_new_worker.end", name));

        isIntroducing = false;
        isMovingToTarget = false;
    }

    private AbstractWorkerEntity findUnknownWorker() {
        List<AbstractWorkerEntity> nearbyWorkers = worker.level().getEntitiesOfClass(
                AbstractWorkerEntity.class,
                worker.getBoundingBox().inflate(CommonConfig.workerDetectionRadius),
                target -> target != worker &&
                        target.isAlive() &&
                        target.getOwner() != null &&
                        target.getOwner().equals(worker.getOwner()) &&
                        !relationships.get().knows(target.getUUID())
        );

        if (nearbyWorkers.isEmpty()) {
            return null;
        }

        return nearbyWorkers.get(worker.getRandom().nextInt(nearbyWorkers.size()));
    }
}
