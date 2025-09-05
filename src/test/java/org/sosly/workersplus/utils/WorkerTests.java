package org.sosly.workersplus.utils;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.NotNull;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.config.CommonConfig;
import org.sosly.workersplus.data.Need;
import org.sosly.workersplus.entities.EntityTypes;
import org.sosly.workersplus.entities.workers.Porter;

import java.util.ArrayList;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(VillageWorkersPlus.MOD_ID)
public class WorkerTests {
    protected static final String BATCH = "WorkerTests";

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testCanWork(final @NotNull GameTestHelper test) {
        Porter worker = test.spawnWithNoFreeWill(EntityTypes.PORTER.get(), 1, 2, 1);
        worker.setStartPos(test.absolutePos(new BlockPos(1, 2, 1)));
        
        worker.setStatus(AbstractWorkerEntity.Status.WORK);
        test.assertTrue(Worker.canWork(worker), "Worker should be able to work when status is WORK");
        
        worker.setStatus(AbstractWorkerEntity.Status.FOLLOW);
        test.assertFalse(Worker.canWork(worker), "Worker should not be able to work when following");
        
        worker.setStatus(AbstractWorkerEntity.Status.WANDER);
        test.assertFalse(Worker.canWork(worker), "Worker should not be able to work when wandering");

        worker.setStatus(AbstractWorkerEntity.Status.WORK);
        test.assertTrue(Worker.canWork(worker), "Worker should be able to work when status is back to WORK");
        
        test.assertFalse(Worker.canWork(null), "Null worker should not be able to work");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetChestContainer(final @NotNull GameTestHelper test) {
        Porter worker = test.spawnWithNoFreeWill(EntityTypes.PORTER.get(), 1, 2, 1);
        BlockPos chestPos = new BlockPos(0, 1, 0);
        
        test.setBlock(chestPos, Blocks.CHEST);
        worker.setChestPos(test.absolutePos(chestPos));
        
        Container chest = Worker.getChestContainer(worker).orElse(null);
        test.assertTrue(chest != null, "Should find chest container at worker's chest position");
        
        worker.setChestPos(BlockPos.ZERO);
        Container noChest = Worker.getChestContainer(worker).orElse(null);
        test.assertTrue(noChest == null, "Should not find chest container at invalid position");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetMainTool(final @NotNull GameTestHelper test) {
        MockWorker worker = new MockWorker(test);
        
        test.assertTrue(Worker.getMainTool(worker).isEmpty(), "Should return empty when inventoryInputHelp is null");
        
        worker.tools = new ArrayList<>();
        test.assertTrue(Worker.getMainTool(worker).isEmpty(), "Should return empty when inventoryInputHelp is empty");
        
        worker.tools.add(Items.IRON_PICKAXE);
        Item mainTool = Worker.getMainTool(worker).orElse(null);
        test.assertTrue(mainTool == Items.IRON_PICKAXE, "Should return first tool when list has at least 1 item");
        
        worker.tools.add(Items.IRON_AXE);
        mainTool = Worker.getMainTool(worker).orElse(null);
        test.assertTrue(mainTool == Items.IRON_PICKAXE, "Should still return first tool when list has 2 items");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetSecondTool(final @NotNull GameTestHelper test) {
        MockWorker worker = new MockWorker(test);
        
        test.assertTrue(Worker.getSecondTool(worker).isEmpty(), "Should return empty when inventoryInputHelp is null");
        
        worker.tools = new ArrayList<>();
        test.assertTrue(Worker.getSecondTool(worker).isEmpty(), "Should return empty when inventoryInputHelp is empty");
        
        worker.tools.add(Items.IRON_PICKAXE);
        test.assertTrue(Worker.getSecondTool(worker).isEmpty(), "Should return empty when list has only 1 item");
        
        worker.tools.add(Items.IRON_AXE);
        Item secondTool = Worker.getSecondTool(worker).orElse(null);
        test.assertTrue(secondTool == Items.IRON_AXE, "Should return second tool when available");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetNeeds(final @NotNull GameTestHelper test) {
        MockWorker worker = new MockWorker(test);
        BlockPos chestPos = new BlockPos(0, 1, 0);
        test.setBlock(chestPos, Blocks.CHEST);
        worker.setChestPos(test.absolutePos(chestPos));
        
        worker.needsMainTool = false;
        worker.needsSecondTool = false;
        
        List<Need> needs = Worker.getNeeds(worker);
        test.assertTrue(needs.size() == 1, "Should have 1 need (food) when no tools needed and no food");
        
        worker.tools = new ArrayList<>();
        worker.tools.add(Items.IRON_PICKAXE);
        worker.tools.add(Items.IRON_AXE);
        worker.needsMainTool = true;
        worker.needsSecondTool = true;
        
        needs = Worker.getNeeds(worker);
        test.assertTrue(needs.size() == 3, "Should have 3 needs (main tool, second tool, food)");
        
        Container chest = Worker.getChestContainer(worker).orElse(null);
        assert chest != null;
        ItemStack bread = new ItemStack(Items.BREAD);
        bread.setCount(CommonConfig.workerFoodThreshold + 10);
        Containers.putItem(chest, bread);
        
        needs = Worker.getNeeds(worker);
        test.assertTrue(needs.size() == 2, "Should have 2 needs (tools only) when enough food is present");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testHasEmptyInventorySlot(final @NotNull GameTestHelper test) {
        Porter worker = test.spawn(EntityTypes.PORTER.get(), 1, 2, 1);
        
        test.assertTrue(Worker.hasEmptyInventorySlot(worker), "New worker should have empty inventory slots");
        
        Container inventory = worker.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            inventory.setItem(i, new ItemStack(Items.DIRT, 1));
        }
        
        test.assertFalse(Worker.hasEmptyInventorySlot(worker), "Worker with full inventory should not have empty slots");
        
        inventory.setItem(0, ItemStack.EMPTY);
        test.assertTrue(Worker.hasEmptyInventorySlot(worker), "Worker should have empty slot after clearing one");
        
        test.assertFalse(Worker.hasEmptyInventorySlot(null), "Null worker should return false");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetNeedsWithOnlyOneToolInList(final @NotNull GameTestHelper test) {
        MockWorker worker = new MockWorker(test);
        
        worker.tools = new ArrayList<>();
        worker.tools.add(Items.IRON_PICKAXE);
        worker.needsMainTool = false;
        worker.needsSecondTool = true;
        
        List<Need> needs = Worker.getNeeds(worker);
        test.assertTrue(needs.size() == 1, "Should have only 1 need (food) when needsSecondTool is true but second tool not available");
        
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testCountFoodInContainer(final @NotNull GameTestHelper test) {
        Porter worker = test.spawn(EntityTypes.PORTER.get(), 1, 2, 1);
        BlockPos chestPos = new BlockPos(0, 1, 0);
        test.setBlock(chestPos, Blocks.CHEST);
        worker.setChestPos(test.absolutePos(chestPos));
        
        Container chest = Worker.getChestContainer(worker).orElse(null);
        assert chest != null;
        
        chest.setItem(0, new ItemStack(Items.BREAD, 10));
        chest.setItem(1, new ItemStack(Items.PUFFERFISH, 5));
        chest.setItem(2, new ItemStack(Items.SWEET_BERRIES, 20));
        chest.setItem(3, new ItemStack(Items.COOKED_BEEF, 15));
        chest.setItem(4, new ItemStack(Items.ROTTEN_FLESH, 8));
        chest.setItem(5, new ItemStack(Items.GOLDEN_CARROT, 7));
        chest.setItem(6, new ItemStack(Items.IRON_INGOT, 64));
        
        List<Need> needs = Worker.getNeeds(worker);
        
        int expectedFoodCount = 32; // Only bread (10), cooked beef (15), and golden carrot (7) should be counted = 32
        boolean hasFoodNeed = needs.stream().anyMatch(need ->
            need.getName().getString().contains("food"));
        
        if (expectedFoodCount >= CommonConfig.workerFoodThreshold) {
            test.assertFalse(hasFoodNeed, "Should not need food when " + expectedFoodCount + " valid food items present");
        } else {
            test.assertTrue(hasFoodNeed, "Should need food when only " + expectedFoodCount + " valid food items present");
        }
        
        test.succeed();
    }

    private static class MockWorker extends Porter {
        public List<Item> tools = null;
        public boolean needsMainTool = false;
        public boolean needsSecondTool = false;
        
        public MockWorker(GameTestHelper test) {
            super(EntityTypes.PORTER.get(), test.getLevel());
            this.setPos(test.absoluteVec(new Vec3(1, 2, 1)));
        }
        
        @Override
        public List<Item> inventoryInputHelp() {
            return tools;
        }
        
        @Override
        public boolean hasAMainTool() {
            return needsMainTool;
        }
        
        @Override
        public boolean hasASecondTool() {
            return needsSecondTool;
        }
    }
}