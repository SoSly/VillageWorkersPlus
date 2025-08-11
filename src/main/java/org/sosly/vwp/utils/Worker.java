package org.sosly.vwp.utils;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.sosly.vwp.data.Need;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker {
    public static boolean canWork(@Nullable AbstractWorkerEntity worker) {
        if (worker == null || !worker.isAlive()) {
            return false;
        }

        return worker.getStatus() != AbstractWorkerEntity.Status.FOLLOW;
    }

    public static Optional<Container> getChestContainer(AbstractWorkerEntity worker) {
        BlockPos pos = worker.getChestPos();
        return Containers.getFromBlockPos(pos, worker.level());
    }

    public static Optional<Item> getMainTool(AbstractWorkerEntity worker) {
        if (worker.inventoryInputHelp() == null || worker.inventoryInputHelp().isEmpty()) {
            return Optional.empty();
        }

        if (worker.inventoryInputHelp().size() < 2) {
            return Optional.empty();
        }

        return Optional.of(worker.inventoryInputHelp().get(0));
    }

    public static Optional<Item> getSecondTool(AbstractWorkerEntity worker) {
        if (worker.inventoryInputHelp() == null || worker.inventoryInputHelp().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(worker.inventoryInputHelp().get(1));
    }

    public static List<Need> getNeeds(AbstractWorkerEntity worker) {
        List<Need> needs = new ArrayList<>();

        if (worker.needsMainTool) {
            getMainTool(worker).ifPresent(tool -> needs.add(Need.item(tool)));
        }

        if (worker.needsSecondTool) {
            getSecondTool(worker).ifPresent(tool -> needs.add(Need.item(tool)));
        }

        // calculate food needs
        AtomicInteger food = new AtomicInteger(countFoodInContainer(worker.getInventory()));
        getChestContainer(worker)
                .ifPresent(container -> food.addAndGet(countFoodInContainer(container)));

        // todo: use a configuration option for the food threshold
        if (food.get() < 3) {
            needs.add(Need.food());
        }

        return needs;
    }

    public static boolean hasEmptyInventorySlot(AbstractWorkerEntity worker) {
        if (worker == null || worker.getInventory() == null) {
            return false;
        }

        for (int i = 0; i < worker.getInventory().getContainerSize(); i++) {
            if (worker.getInventory().getItem(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static int countFoodInContainer(Container container) {
        int food = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem().isEdible()) {
                food += stack.getCount();
            }
        }

        return food;
    }
}
