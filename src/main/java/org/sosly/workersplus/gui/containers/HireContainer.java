package org.sosly.workersplus.gui.containers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.inventory.WorkerHireContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HireContainer extends WorkerHireContainer {
    public HireContainer(int id, Player player, AbstractWorkerEntity worker, Inventory inv) {
        super(id, player, worker, inv);
    }
}
