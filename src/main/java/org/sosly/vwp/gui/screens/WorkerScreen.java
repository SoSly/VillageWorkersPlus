package org.sosly.vwp.gui.screens;

import com.talhanation.workers.client.gui.WorkerInventoryScreen;
import com.talhanation.workers.inventory.WorkerInventoryContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorkerScreen extends WorkerInventoryScreen {
    public WorkerScreen(WorkerInventoryContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }
}
