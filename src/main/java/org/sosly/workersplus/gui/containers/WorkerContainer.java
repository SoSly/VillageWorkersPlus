package org.sosly.workersplus.gui.containers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.inventory.WorkerInventoryContainer;
import net.minecraft.world.entity.player.Inventory;

public class WorkerContainer extends WorkerInventoryContainer {
    public WorkerContainer(int id, AbstractWorkerEntity worker, Inventory inv) {
        super(id, worker, inv);
    }
}
