package org.sosly.workersplus.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.sosly.workersplus.data.ItemPredicate;

import java.util.Optional;

public class Containers {

    public static boolean hasItem(Container container, ItemPredicate predicate) {
        if (container == null) {
            return false;
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (predicate.matches(container.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public static Optional<Container> getFromBlockPos(BlockPos pos, Level level) {
        if (pos == null || !level.isLoaded(pos)) {
            return Optional.empty();
        }

        BlockEntity entity = level.getBlockEntity(pos);
        
        if (entity instanceof ChestBlockEntity) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ChestBlock chestBlock) {
                Container chestContainer = ChestBlock.getContainer(chestBlock, state, level, pos, true);
                if (chestContainer != null) {
                    return Optional.of(chestContainer);
                }
            }
        }
        
        if (entity instanceof Container container) {
            return Optional.of(container);
        }

        return Optional.empty();
    }

    public static ItemStack putItem(Container container, ItemStack stack) {
        if (container == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack existingStack = container.getItem(i);
            if (existingStack.isEmpty()) {
                int amountToPlace = Math.min(stack.getMaxStackSize(), stack.getCount());
                container.setItem(i, stack.split(amountToPlace));
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemStack.isSameItem(existingStack, stack)) {
                int space = Math.min(existingStack.getMaxStackSize() - existingStack.getCount(), stack.getCount());
                if (space > 0) {
                    existingStack.grow(space);
                    stack.shrink(space);
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        return stack;
    }

    public static ItemStack removeItem(Container container, ItemPredicate predicate, int maxCount) {
        if (container == null) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (predicate.matches(stack)) {
                int countToRemove = Math.min(maxCount, stack.getCount());
                ItemStack removedStack = stack.split(countToRemove);
                if (stack.isEmpty()) {
                    container.setItem(i, ItemStack.EMPTY);
                }
                return removedStack;
            }
        }

        return ItemStack.EMPTY;
    }
}
