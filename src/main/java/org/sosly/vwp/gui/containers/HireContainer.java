package org.sosly.vwp.gui.containers;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.inventory.WorkerHireContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class HireContainer extends WorkerHireContainer {
    public HireContainer(int id, Player playerEntity, AbstractWorkerEntity worker, Inventory playerInventory) {
        super(id, playerEntity, worker, playerInventory);
    }
}
