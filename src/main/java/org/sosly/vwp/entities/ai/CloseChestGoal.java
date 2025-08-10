package org.sosly.vwp.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.function.Supplier;

public class CloseChestGoal extends Goal {
    private final Porter porter;
    private final Supplier<BlockPos> chestPosSupplier;
    private final Supplier<Boolean> shouldClose;
    private final Runnable onClosed;
    
    public CloseChestGoal(Porter porter, Supplier<BlockPos> chestPosSupplier,
                         Supplier<Boolean> shouldClose, Runnable onClosed) {
        this.porter = porter;
        this.chestPosSupplier = chestPosSupplier;
        this.shouldClose = shouldClose;
        this.onClosed = onClosed;
    }
    
    @Override
    public boolean canUse() {
        if (!shouldClose.get()) {
            return false;
        }
        
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || !task.isChestOpened()) {
            return false;
        }
        
        return task.getOpenContainer() != null;
    }
    
    @Override
    public boolean canContinueToUse() {
        return false;
    }
    
    @Override
    public void start() {
        DeliveryTask task = porter.getDeliveryTask();
        Container container = task.getOpenContainer();
        BlockPos chestPos = chestPosSupplier.get();
        
        if (container != null && chestPos != null) {
            closeChest(container, chestPos);
        }
        
        task.setOpenContainer(null);
        task.setChestOpened(false);
        
        if (onClosed != null) {
            onClosed.run();
        }
    }
    
    private void closeChest(Container container, BlockPos chestPos) {
        if (!(container instanceof CompoundContainer || container instanceof ChestBlockEntity)) {
            return;
        }
        
        BlockState state = porter.level().getBlockState(chestPos);
        Block block = state.getBlock();
        
        porter.level().blockEvent(chestPos, block, 1, 0);
        porter.level().playSound(null, chestPos, SoundEvents.CHEST_CLOSE,
            porter.getSoundSource(), 0.7F, 0.8F + 0.4F * porter.getRandom().nextFloat());
        porter.level().gameEvent(porter, GameEvent.BLOCK_CLOSE, chestPos);
    }
}