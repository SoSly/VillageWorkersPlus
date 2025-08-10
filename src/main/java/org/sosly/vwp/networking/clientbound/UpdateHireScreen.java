package org.sosly.vwp.networking.clientbound;

import com.talhanation.workers.client.gui.WorkerHireScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.sosly.vwp.networking.Message;

public class UpdateHireScreen implements Message<UpdateHireScreen> {
    public ItemStack currency;
    public int amount;

    public UpdateHireScreen() {}

    public UpdateHireScreen(ItemStack currency, int amount) {
        this.currency = currency;
        this.amount = amount;
    }

    @Override
    public LogicalSide receiver() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        WorkerHireScreen.currency = this.currency;
        WorkerHireScreen.amount = this.amount;
    }

    @Override
    public UpdateHireScreen fromBytes(FriendlyByteBuf buf) {
        this.currency = buf.readItem();
        this.amount = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItemStack(currency, false);
        buf.writeInt(amount);
    }
}