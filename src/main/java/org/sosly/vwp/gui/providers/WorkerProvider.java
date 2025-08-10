package org.sosly.vwp.gui.providers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.sosly.vwp.gui.containers.WorkerContainer;

public class WorkerProvider<T extends AbstractWorkerEntity> implements MenuProvider {
    private final T worker;

    public WorkerProvider(T worker) {
        this.worker = worker;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return worker.getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new WorkerContainer(id, worker, inv);
    }

}
