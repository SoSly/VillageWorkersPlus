package org.sosly.vwp.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.sosly.vwp.entities.workers.Porter;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.function.Supplier;

public class OpenChestGoal extends Goal {
    private final Porter porter;
    private final Supplier<BlockPos> chestPosSupplier;
    private final Supplier<Boolean> shouldOpen;
    private final Runnable onOpened;
    
    public OpenChestGoal(Porter porter, Supplier<BlockPos> chestPosSupplier, 
                        Supplier<Boolean> shouldOpen, Runnable onOpened) {
        this.porter = porter;
        this.chestPosSupplier = chestPosSupplier;
        this.shouldOpen = shouldOpen;
        this.onOpened = onOpened;
    }
    
    @Override
    public boolean canUse() {
        if (!shouldOpen.get()) {
            return false;
        }
        
        DeliveryTask task = porter.getDeliveryTask();
        if (task == null || task.isChestOpened()) {
            return false;
        }
        
        BlockPos chestPos = chestPosSupplier.get();
        if (chestPos == null) {
            return false;
        }
        
        double distanceSq = porter.distanceToSqr(
            chestPos.getX() + 0.5,
            chestPos.getY(),
            chestPos.getZ() + 0.5
        );
        
        return distanceSq <= 9.0;
    }
    
    @Override
    public boolean canContinueToUse() {
        return false;
    }
    
    @Override
    public void start() {
        BlockPos chestPos = chestPosSupplier.get();
        if (chestPos == null) {
            return;
        }
        
        Container container = getContainer(chestPos);
        if (container == null) {
            return;
        }
        
        openChest(container, chestPos);
        
        DeliveryTask task = porter.getDeliveryTask();
        task.setOpenContainer(container);
        task.setChestOpened(true);
        
        if (onOpened != null) {
            onOpened.run();
        }
    }
    
    private Container getContainer(BlockPos chestPos) {
        BlockEntity entity = porter.level().getBlockEntity(chestPos);
        BlockState blockState = porter.level().getBlockState(chestPos);
        
        if (blockState.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, blockState, porter.level(), chestPos, false);
        }
        
        if (entity instanceof Container containerEntity) {
            return containerEntity;
        }
        
        return null;
    }
    
    private void openChest(Container container, BlockPos chestPos) {
        if (!(container instanceof CompoundContainer || container instanceof ChestBlockEntity)) {
            return;
        }
        
        BlockState state = porter.level().getBlockState(chestPos);
        Block block = state.getBlock();
        
        porter.level().blockEvent(chestPos, block, 1, 1);
        porter.level().playSound(null, chestPos, SoundEvents.CHEST_OPEN, 
            porter.getSoundSource(), 0.7F, 0.8F + 0.4F * porter.getRandom().nextFloat());
        porter.level().gameEvent(porter, GameEvent.BLOCK_OPEN, chestPos);
    }
}