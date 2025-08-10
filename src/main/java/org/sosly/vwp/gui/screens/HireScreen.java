package org.sosly.vwp.gui.screens;

import com.talhanation.workers.client.gui.WorkerHireScreen;
import com.talhanation.workers.inventory.WorkerHireContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HireScreen extends WorkerHireScreen {
    public HireScreen(WorkerHireContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
    }
}
