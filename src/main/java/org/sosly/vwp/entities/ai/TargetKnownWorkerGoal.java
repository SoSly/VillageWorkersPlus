package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.data.WorkerRelationships;
import org.sosly.vwp.tasks.AbstractTask;
import org.sosly.vwp.utils.Chat;
import org.sosly.vwp.utils.Worker;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TargetKnownWorkerGoal extends AbstractTaskGoal {
    private final Supplier<WorkerRelationships> relationships;
    private final AbstractWorkerEntity worker;
    private final boolean useRecencyCheck;
    private final Map<UUID, Long> lastSelectTimes = new HashMap<>();
    private static final long RECENCY_THRESHOLD = 1200L;

    public TargetKnownWorkerGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task, Supplier<WorkerRelationships> relationships) {
        this(worker, task, relationships, true);
    }

    public TargetKnownWorkerGoal(AbstractWorkerEntity worker, Supplier<AbstractTask<?>> task, Supplier<WorkerRelationships> relationships, boolean useRecencyCheck) {
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

        if (relationships.get().getRelationships().isEmpty()) {
            return false;
        }

        return getTask().isCurrentStep(this);
    }

    @Override
    public void start() {
        long currentTime = worker.level().getGameTime();
        
        List<AbstractWorkerEntity> availableWorkers = relationships.get().getRelationships()
                .keySet()
                .stream()
                .filter(uuid -> !uuid.equals(worker.getUUID()))
                .filter(uuid -> !useRecencyCheck || !isRecentlySelected(uuid, currentTime))
                .map(uuid -> {
                    AbstractWorkerEntity entity = (AbstractWorkerEntity) worker.getServer().overworld().getEntity(uuid);
                    if (!Worker.canWork(entity)) {
                        return null;
                    }

                    return entity;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (availableWorkers.isEmpty()) {
            getTask().reset();
            return;
        }

        AbstractWorkerEntity target = availableWorkers.get(worker.getRandom().nextInt(availableWorkers.size()));
        if (target == null) {
            getTask().reset();
            return;
        }

        if (useRecencyCheck) {
            lastSelectTimes.put(target.getUUID(), currentTime);
        }

        getTask().setData("target", target);
        getTask().next();
        Chat.send(worker, Component.translatable("chat.vwp.selecting_worker.success", target.getName()));
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
