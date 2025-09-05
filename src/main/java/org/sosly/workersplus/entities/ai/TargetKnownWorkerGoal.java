package org.sosly.workersplus.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.config.CommonConfig;
import org.sosly.workersplus.data.WorkerInfo;
import org.sosly.workersplus.data.WorkerRelationships;
import org.sosly.workersplus.tasks.AbstractTask;
import org.sosly.workersplus.utils.Chat;
import org.sosly.workersplus.utils.Worker;

import java.util.*;
import java.util.stream.Collectors;

public class TargetKnownWorkerGoal extends AbstractTaskGoal {
    private final WorkerRelationships relationships;
    private final AbstractWorkerEntity worker;
    private final boolean useRecencyCheck;
    private final Map<UUID, Long> lastSelectTimes = new HashMap<>();
    private static final long RECENCY_THRESHOLD = 1200L;
    private AbstractWorkerEntity selectedTarget = null;

    public TargetKnownWorkerGoal(AbstractWorkerEntity worker, AbstractTask<?> task, WorkerRelationships relationships) {
        this(worker, task, relationships, true);
    }

    public TargetKnownWorkerGoal(AbstractWorkerEntity worker, AbstractTask<?> task, WorkerRelationships relationships, boolean useRecencyCheck) {
        super(task);
        this.relationships = relationships;
        this.worker = worker;
        this.useRecencyCheck = useRecencyCheck;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Worker.canWork(worker)) {
            return false;
        }

        if (isStuckInCurrentStep()) {
            VillageWorkersPlus.LOGGER.warn("Worker {} is stuck in TargetKnownWorkerGoal, resetting task.",
                    worker.getUUID());
            getTask().reset();
            return false;
        }

        if (!getTask().isCurrentStep(this)) {
            return false;
        }

        if (relationships.getRelationships().isEmpty()) {
            getTask().reset();
            return false;
        }
        
        selectedTarget = findValidTarget();
        if (selectedTarget == null) {
            getTask().reset();
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (selectedTarget == null) {
            getTask().reset();
            return;
        }

        if (useRecencyCheck) {
            lastSelectTimes.put(selectedTarget.getUUID(), worker.level().getGameTime());
        }

        getTask().setData("target", selectedTarget);
        Chat.send(worker, Component.translatable("chat.workersplus.selecting_worker.success", selectedTarget.getName()));
        getTask().next();
        
        selectedTarget = null;
    }

    private AbstractWorkerEntity findValidTarget() {
        long currentTime = worker.level().getGameTime();
        
        List<UUID> candidates = relationships.getRelationships()
                .keySet()
                .stream()
                .filter(uuid -> !uuid.equals(worker.getUUID()))
                .filter(uuid -> !useRecencyCheck || !isRecentlySelected(uuid, currentTime))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return null;
        }
        
        UUID selectedId = candidates.get(worker.getRandom().nextInt(candidates.size()));
        AbstractWorkerEntity entity = (AbstractWorkerEntity) worker.getServer().overworld().getEntity(selectedId);
        
        if (entity == null || !entity.isAlive()) {
            WorkerInfo forgottenWorker = relationships.getRelationships().get(selectedId);
            if (forgottenWorker != null) {
                Chat.send(worker, Component.translatable("chat.workersplus.selecting_worker.failure", forgottenWorker.getName()));
                relationships.removeKnown(selectedId);
            }
            return null;
        }
        
        if (worker.distanceToSqr(entity) > CommonConfig.workerDetectionRadius * CommonConfig.workerDetectionRadius) {
            return null;
        }
        
        return entity;
    }

    private boolean isRecentlySelected(UUID workerId, long currentTime) {
        Long lastVisit = lastSelectTimes.get(workerId);
        return lastVisit != null && (currentTime - lastVisit) < RECENCY_THRESHOLD;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
