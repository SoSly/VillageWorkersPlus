package org.sosly.vwp.entities.ai;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.entities.workers.Porter;

import java.util.List;
import java.util.UUID;

public class MeetNewWorkerGoal extends Goal {
    private final Porter porter;
    private AbstractWorkerEntity targetWorker;
    private long lastScanTime;
    private static final long SCAN_COOLDOWN = 100L;
    private long introductionStartTime;
    private boolean isIntroducing;
    
    public MeetNewWorkerGoal(Porter porter) {
        this.porter = porter;
        this.lastScanTime = 0;
        this.introductionStartTime = 0;
        this.isIntroducing = false;
    }
    
    @Override
    public boolean canUse() {
        if (porter.getStatus() != AbstractWorkerEntity.Status.WORK &&
            porter.getStatus() != AbstractWorkerEntity.Status.IDLE) {
            return false;
        }
        
        long currentTime = porter.level().getGameTime();
        
        if (currentTime - lastScanTime < SCAN_COOLDOWN) {
            return false;
        }
        
        lastScanTime = currentTime;
        
        targetWorker = findUnknownWorker();
        return targetWorker != null;
    }
    
    @Override
    public boolean canContinueToUse() {
        if (targetWorker == null || !targetWorker.isAlive()) {
            return false;
        }
        
        if (porter.getStatus() != AbstractWorkerEntity.Status.WORK &&
            porter.getStatus() != AbstractWorkerEntity.Status.IDLE) {
            return false;
        }
        
        return isIntroducing || porter.distanceToSqr(targetWorker) > CommonConfig.porterChatDistance * CommonConfig.porterChatDistance;
    }
    
    @Override
    public void start() {
        introductionStartTime = 0;
        isIntroducing = false;
        
        porter.chat(Component.translatable("chat.vwp.porter.meeting_new_worker"));
        porter.getNavigation().moveTo(targetWorker, 1.0D);
    }
    
    @Override
    public void stop() {
        if (isIntroducing && targetWorker != null) {
            UUID workerId = targetWorker.getUUID();
            String workerName = targetWorker.getName().getString();
            net.minecraft.core.BlockPos workerWorkPos = targetWorker.getStartPos();
            porter.addKnownWorker(workerId, workerName, workerWorkPos);
            porter.chat(Component.translatable("chat.vwp.porter.met_worker", workerName));
            if (porter.getStartPos() != null) {
                porter.getNavigation().moveTo(porter.getStartPos().getX() + 0.5, 
                                             porter.getStartPos().getY(), 
                                             porter.getStartPos().getZ() + 0.5, 1.0);
            }
        }
        
        targetWorker = null;
        introductionStartTime = 0;
        isIntroducing = false;
    }
    
    @Override
    public void tick() {
        if (targetWorker == null) {
            return;
        }
        
        double distanceSq = porter.distanceToSqr(targetWorker);
        double chatDistanceSq = CommonConfig.porterChatDistance * CommonConfig.porterChatDistance;
        
        if (distanceSq <= chatDistanceSq) {
            if (!isIntroducing) {
                isIntroducing = true;
                introductionStartTime = porter.level().getGameTime();
                porter.getNavigation().stop();
            }
            
            porter.getLookControl().setLookAt(targetWorker, 10.0F, (float)porter.getMaxHeadXRot());

            long currentTime = porter.level().getGameTime();
            long timeSpent = currentTime - introductionStartTime;
            
            if (timeSpent >= CommonConfig.porterIntroductionTime * 20) {
                this.stop();
            }
        } else if (!isIntroducing) {
            if (porter.getNavigation().isDone()) {
                porter.getNavigation().moveTo(targetWorker, 1.0D);
            }
        }
    }
    
    private AbstractWorkerEntity findUnknownWorker() {
        List<AbstractWorkerEntity> nearbyWorkers = porter.level().getEntitiesOfClass(
            AbstractWorkerEntity.class,
            porter.getBoundingBox().inflate(CommonConfig.porterScanRadius),
            worker -> worker != porter && 
                      worker.isAlive() && 
                      worker.getOwner() != null &&
                      worker.getOwner().equals(porter.getOwner())
        );
        
        for (AbstractWorkerEntity worker : nearbyWorkers) {
            if (!porter.knowsWorker(worker.getUUID())) {
                return worker;
            }
        }
        
        return null;
    }
}