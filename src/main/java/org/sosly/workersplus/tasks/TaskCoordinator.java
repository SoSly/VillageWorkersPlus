package org.sosly.workersplus.tasks;

import org.sosly.workersplus.VillageWorkersPlus;

public class TaskCoordinator {
    private AbstractTask<?> activeTask;
    
    public boolean canStartTask(AbstractTask<?> task) {
        if (activeTask == null) {
            return true;
        }
        
        if (activeTask == task) {
            return true;
        }
        
        if (!activeTask.isActive()) {
            activeTask = null;
            return true;
        }
        
        return false;
    }
    
    public void setActiveTask(AbstractTask<?> task) {
        if (activeTask != null && activeTask != task && activeTask.isActive()) {
            VillageWorkersPlus.LOGGER.warn("Setting new active task while another is still active: {} -> {}", 
                activeTask.getClass().getSimpleName(), task.getClass().getSimpleName());
        }
        activeTask = task;
    }
    
    public void clearActiveTask(AbstractTask<?> task) {
        if (activeTask == task) {
            activeTask = null;
        }
    }
    
    public AbstractTask<?> getActiveTask() {
        return activeTask;
    }
    
    public boolean hasActiveTask() {
        return activeTask != null && activeTask.isActive();
    }
}