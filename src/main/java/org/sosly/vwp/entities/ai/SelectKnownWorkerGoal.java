package org.sosly.vwp.entities.ai;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.data.WorkerInfo;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SelectKnownWorkerGoal extends Goal {
    private final Porter porter;
    private final Random random = new Random();
    private long lastSelectionTime;
    private static final long SELECTION_COOLDOWN = 100L;
    
    public SelectKnownWorkerGoal(Porter porter) {
        this.porter = porter;
        this.lastSelectionTime = 0;
    }
    
    @Override
    public boolean canUse() {
        if (porter.getOwner() == null) {
            return false;
        }
        
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isInState(DeliveryTask.State.IDLE)) {
            return false;
        }
        
        long currentTime = porter.level().getGameTime();
        if (currentTime - lastSelectionTime < SELECTION_COOLDOWN) {
            return false;
        }
        
        List<WorkerInfo> knownWorkers = porter.getKnownWorkers();
        return !knownWorkers.isEmpty();
    }
    
    @Override
    public boolean canContinueToUse() {
        return false;
    }
    
    @Override
    public void start() {
        List<WorkerInfo> knownWorkers = porter.getKnownWorkers();
        if (knownWorkers.isEmpty()) {
            return;
        }
        
        DeliveryTask task = porter.getDeliveryTask();
        long currentTime = porter.level().getGameTime();
        long visitCooldown = CommonConfig.porterVisitInterval * 60L * 20L;
        
        List<WorkerInfo> eligibleWorkers = new ArrayList<>();
        for (WorkerInfo worker : knownWorkers) {
            if (!task.wasRecentlyVisited(worker.getId(), currentTime, visitCooldown)) {
                eligibleWorkers.add(worker);
            }
        }
        
        if (eligibleWorkers.isEmpty()) {
            return;
        }
        
        WorkerInfo selectedWorker = selectRandomWorker(eligibleWorkers);
        if (selectedWorker == null) {
            return;
        }

        task.setTargetWorker(selectedWorker);
        task.transitionTo(DeliveryTask.State.NAVIGATING_TO_WORKER);
        porter.chat(Component.translatable("chat.vwp.porter.wondering_about_worker", selectedWorker.getName()));

        lastSelectionTime = currentTime;
    }
    
    private WorkerInfo selectRandomWorker(List<WorkerInfo> workers) {
        if (workers.isEmpty()) {
            return null;
        }
        
        int index = random.nextInt(workers.size());
        return workers.get(index);
    }
}