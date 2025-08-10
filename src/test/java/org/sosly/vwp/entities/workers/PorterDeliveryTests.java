package org.sosly.vwp.entities.workers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.NotNull;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.data.WorkerInfo;
import org.sosly.vwp.entities.EntityTypes;
import org.sosly.vwp.tasks.DeliveryTask;

import java.util.UUID;

@PrefixGameTestTemplate(false)
@GameTestHolder(VillageWorkersPlus.MOD_ID)
public class PorterDeliveryTests {
    private static final String BATCH = "porter_delivery_tests";
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testDeliveryTaskStateTransitions(final @NotNull GameTestHelper test) {
        Entity porterEntity = test.spawn(EntityTypes.PORTER.get(), 2, 2, 2);
        test.assertTrue(porterEntity instanceof Porter, "Entity is not a Porter");
        
        Porter porter = (Porter) porterEntity;
        DeliveryTask task = porter.getDeliveryTask();
        
        test.assertTrue(task.isInState(DeliveryTask.State.IDLE), "Task should start in IDLE state");
        
        task.transitionTo(DeliveryTask.State.SELECTING_WORKER);
        test.assertTrue(task.isInState(DeliveryTask.State.SELECTING_WORKER), "Task should be in SELECTING_WORKER state");
        
        task.transitionTo(DeliveryTask.State.NAVIGATING_TO_WORKER);
        test.assertTrue(task.isInState(DeliveryTask.State.NAVIGATING_TO_WORKER), "Task should be in NAVIGATING_TO_WORKER state");
        
        task.reset();
        test.assertTrue(task.isInState(DeliveryTask.State.IDLE), "Task should reset to IDLE state");
        
        test.succeed();
    }
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testWorkerSelection(final @NotNull GameTestHelper test) {
        Entity porterEntity = test.spawn(EntityTypes.PORTER.get(), 2, 2, 2);
        Porter porter = (Porter) porterEntity;
        
        UUID workerId = UUID.randomUUID();
        BlockPos workerPos = new BlockPos(5, 2, 5);
        porter.addKnownWorker(workerId, "TestWorker", workerPos);
        
        test.assertTrue(porter.knowsWorker(workerId), "Porter should know the worker");
        test.assertTrue(porter.getKnownWorkers().size() == 1, "Porter should have exactly one known worker");
        
        WorkerInfo workerInfo = porter.getKnownWorkers().get(0);
        test.assertTrue(workerInfo.getId().equals(workerId), "Worker ID should match");
        test.assertTrue(workerInfo.getName().equals("TestWorker"), "Worker name should match");
        test.assertTrue(workerInfo.getWorkPosition().equals(workerPos), "Worker position should match");
        
        test.succeed();
    }
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testChestInteraction(final @NotNull GameTestHelper test) {
        Entity porterEntity = test.spawn(EntityTypes.PORTER.get(), 2, 2, 2);
        Porter porter = (Porter) porterEntity;
        
        BlockPos chestPos = new BlockPos(3, 2, 3);
        test.setBlock(chestPos, Blocks.CHEST);
        
        ChestBlockEntity chest = (ChestBlockEntity) test.getBlockEntity(chestPos);
        test.assertTrue(chest != null, "Chest block entity should exist");
        
        chest.setItem(0, new ItemStack(Items.BREAD, 10));
        
        DeliveryTask task = porter.getDeliveryTask();
        task.setOwnChestPos(chestPos);
        
        test.assertTrue(task.getOwnChestPos().equals(chestPos), "Own chest position should be set");
        
        test.succeed();
    }
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testDeliveryTaskPersistence(final @NotNull GameTestHelper test) {
        Entity porterEntity = test.spawn(EntityTypes.PORTER.get(), 2, 2, 2);
        Porter porter = (Porter) porterEntity;
        
        DeliveryTask task = porter.getDeliveryTask();
        BlockPos testPos = new BlockPos(10, 5, 10);
        
        task.transitionTo(DeliveryTask.State.COLLECTING_ITEMS);
        task.setOwnChestPos(testPos);
        task.setHomePos(new BlockPos(2, 2, 2));
        
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        task.save(tag);
        
        DeliveryTask loadedTask = new DeliveryTask();
        loadedTask.load(tag);
        
        test.assertTrue(loadedTask.isInState(DeliveryTask.State.COLLECTING_ITEMS), "State should persist");
        test.assertTrue(loadedTask.getOwnChestPos().equals(testPos), "Own chest position should persist");
        test.assertTrue(loadedTask.getHomePos().equals(new BlockPos(2, 2, 2)), "Home position should persist");
        
        test.succeed();
    }
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testNavigationGoalActivation(final @NotNull GameTestHelper test) {
        Entity porterEntity = test.spawn(EntityTypes.PORTER.get(), 2, 2, 2);
        Porter porter = (Porter) porterEntity;
        
        porter.setStartPos(new BlockPos(2, 2, 2));
        
        UUID workerId = UUID.randomUUID();
        BlockPos workerPos = new BlockPos(10, 2, 10);
        porter.addKnownWorker(workerId, "DistantWorker", workerPos);
        
        DeliveryTask task = porter.getDeliveryTask();
        WorkerInfo workerInfo = porter.getKnownWorkers().get(0);
        task.setTargetWorker(workerInfo);
        task.transitionTo(DeliveryTask.State.NAVIGATING_TO_WORKER);
        
        test.succeedWhen(() -> {
            test.assertTrue(task.getTargetWorker() != null, "Target worker should be set");
            test.assertTrue(task.isInState(DeliveryTask.State.NAVIGATING_TO_WORKER), "Should be navigating to worker");
        });
    }
}