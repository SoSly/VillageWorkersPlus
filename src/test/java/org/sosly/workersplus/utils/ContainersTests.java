package org.sosly.workersplus.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.data.ItemPredicate;
import org.sosly.workersplus.data.Need;

@PrefixGameTestTemplate(false)
@GameTestHolder(VillageWorkersPlus.MOD_ID)
public class ContainersTests {
    protected static final String BATCH = "ContainersTests";

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testGetFromBlockPos(final GameTestHelper test) {
        BlockPos chestPos = new BlockPos(0,1,0);
        test.setBlock(chestPos, Blocks.CHEST);

        Container chest = Containers.getFromBlockPos(test.absolutePos(chestPos), test.getLevel()).orElse(null);
        test.assertTrue(chest != null, "Chest container not found");
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testPutItem(final GameTestHelper test) {
        BlockPos chestPos = new BlockPos(0,1,0);
        test.setBlock(chestPos, Blocks.CHEST);

        Container chest = Containers.getFromBlockPos(test.absolutePos(chestPos), test.getLevel()).orElse(null);
        test.assertTrue(chest != null, "Chest container not found");
        assert chest != null;

        ItemStack bread = new ItemStack(Items.BREAD);
        bread.setCount(10);

        Containers.putItem(chest, bread);
        test.assertTrue(chest.getItem(0).getItem() == Items.BREAD, "Item not put in chest");
        test.assertTrue(chest.getItem(0).getCount() == 10, "Item count mismatch in chest");
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testHasItem(final GameTestHelper test) {
        BlockPos chestPos = new BlockPos(0,1,0);
        test.setBlock(chestPos, Blocks.CHEST);

        Container chest = Containers.getFromBlockPos(test.absolutePos(chestPos), test.getLevel()).orElse(null);
        test.assertTrue(chest != null, "Chest container not found");
        assert chest != null;

        ItemStack bread = new ItemStack(Items.BREAD);
        bread.setCount(10);

        ItemPredicate predicate = new Need(itemStack -> itemStack.getItem() == Items.BREAD, 10, bread.getDisplayName());
        test.assertFalse(Containers.hasItem(chest, predicate), "Chest should not have bread yet");

        Containers.putItem(chest, bread);
        test.assertTrue(Containers.hasItem(chest, predicate), "Chest should have bread now");
        test.succeed();
    }

    @GameTest(template = "empty", batch = BATCH, attempts = 3, requiredSuccesses = 3)
    public static void testRemoveItem(final GameTestHelper test) {
        BlockPos chestPos = new BlockPos(0,1,0);
        test.setBlock(chestPos, Blocks.CHEST);

        Container chest = Containers.getFromBlockPos(test.absolutePos(chestPos), test.getLevel()).orElse(null);
        test.assertTrue(chest != null, "Chest container not found");
        assert chest != null;

        ItemStack bread = new ItemStack(Items.BREAD);
        bread.setCount(10);
        Containers.putItem(chest, bread);

        ItemPredicate predicate = new Need(itemStack -> itemStack.getItem() == Items.BREAD, 10, bread.getDisplayName());
        test.assertTrue(Containers.hasItem(chest, predicate), "Chest should have bread");

        ItemStack first = Containers.removeItem(chest, predicate, 5);
        test.assertTrue(Containers.hasItem(chest, predicate), "Chest should still have bread after removing some");
        test.assertTrue(first.getItem() == Items.BREAD, "Removed item should be bread");
        test.assertTrue(first.getCount() == 5, "Removed item count should be 5");
        test.assertTrue(chest.getItem(0).getCount() == 5, "Chest should have 5 bread remaining after first removal");

        ItemStack second = Containers.removeItem(chest, predicate, 5);
        test.assertFalse(Containers.hasItem(chest, predicate), "Chest should not have bread after removing all");
        test.assertTrue(second.getItem() == Items.BREAD, "Removed item should be bread");
        test.assertTrue(second.getCount() == 5, "Removed item count should be 5");
        test.assertTrue(chest.getItem(0).isEmpty(), "Chest slot should be empty after removing all");

        ItemStack third = Containers.removeItem(chest, predicate, 1);
        test.assertTrue(third.isEmpty(), "Removing from empty chest should return empty stack");
        test.assertFalse(Containers.hasItem(chest, predicate), "Chest should still be empty after trying to remove from it");

        test.succeed();
    }
}
