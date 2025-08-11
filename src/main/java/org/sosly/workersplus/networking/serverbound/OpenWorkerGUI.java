package org.sosly.workersplus.networking.serverbound;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.sosly.workersplus.networking.Message;

import java.util.UUID;

public class OpenWorkerGUI implements Message<OpenWorkerGUI> {
    private UUID player;
    private UUID worker;

    public OpenWorkerGUI() {
        this.player = new UUID(0, 0);
    }

    public OpenWorkerGUI(Player player, UUID worker) {
        this.player = player.getUUID();
        this.worker = worker;
    }

    @Override
    public LogicalSide receiver() {
        return LogicalSide.SERVER;
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (!context.getSender().getUUID().equals(player)) {
            return;
        }

        context.getSender().getCommandSenderWorld()
                .getEntitiesOfClass(AbstractWorkerEntity.class, context.getSender().getBoundingBox().inflate(16.0D),
                        v -> v.getUUID().equals(worker))
                .stream()
                .filter(AbstractWorkerEntity::isAlive)
                .findAny()
                .ifPresent(workerEntity -> workerEntity.openGUI(context.getSender()));
    }

    public OpenWorkerGUI fromBytes(FriendlyByteBuf buf) {
        OpenWorkerGUI msg = new OpenWorkerGUI();

        UUID player = buf.readUUID();
        UUID worker = buf.readUUID();

        msg.player = player;
        msg.worker = worker;

        return msg;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeUUID(worker);
    }
}
