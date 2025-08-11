package org.sosly.workersplus.gui.providers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.sosly.workersplus.gui.containers.HireContainer;

public class HireProvider<T extends AbstractWorkerEntity> implements MenuProvider {
    T worker;

    public HireProvider(T worker) {
        this.worker = worker;
    }

    public @NotNull Component getDisplayName() {
        return worker.getName();
    }

    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new HireContainer(id, player, worker, inv);
    }
}
