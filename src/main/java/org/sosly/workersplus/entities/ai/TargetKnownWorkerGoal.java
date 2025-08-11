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

        if (relationships.getRelationships().isEmpty()) {
            return false;
        }

        return getTask().isCurrentStep(this);
    }

    @Override
    public void start() {
        long currentTime = worker.level().getGameTime();
        
        List<UUID> knownWorkerIds = relationships.getRelationships()
                .keySet()
                .stream()
                .filter(uuid -> !uuid.equals(worker.getUUID()))
                .filter(uuid -> !useRecencyCheck || !isRecentlySelected(uuid, currentTime))
                .collect(Collectors.toList());

        if (knownWorkerIds.isEmpty()) {
            getTask().reset();
            return;
        }

        AbstractWorkerEntity target = null;
        UUID selectedId = null;
        
        while (!knownWorkerIds.isEmpty() && target == null) {
            selectedId = knownWorkerIds.get(worker.getRandom().nextInt(knownWorkerIds.size()));
            
            AbstractWorkerEntity entity = (AbstractWorkerEntity) worker.getServer().overworld().getEntity(selectedId);
            
            if (entity != null && entity.isAlive() && 
                worker.distanceToSqr(entity) <= CommonConfig.workerDetectionRadius * CommonConfig.workerDetectionRadius) {
                if (Worker.canWork(entity)) {
                    target = entity;
                }
            } else {
                WorkerInfo forgottenWorker = relationships.getRelationships().get(selectedId);
                if (forgottenWorker != null) {
                    Chat.send(worker, Component.translatable("chat.workersplus.selecting_worker.failure", forgottenWorker.getName()));
                    relationships.removeKnown(selectedId);
                }
                knownWorkerIds.remove(selectedId);
            }
        }

        if (target == null) {
            getTask().reset();
            return;
        }

        if (useRecencyCheck) {
            lastSelectTimes.put(target.getUUID(), currentTime);
        }

        getTask().setData("target", target);
        getTask().next();
        Chat.send(worker, Component.translatable("chat.workersplus.selecting_worker.success", target.getName()));
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
