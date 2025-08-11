package org.sosly.workersplus.networking.serverbound;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.sosly.workersplus.networking.Message;

import java.util.Objects;
import java.util.UUID;

public class OpenHireGUI implements Message<OpenHireGUI> {

    private UUID player;
    private UUID worker;


    public OpenHireGUI() {
        this.player = new UUID(0, 0);
    }

    public OpenHireGUI(Player player, UUID worker) {
        this.player = player.getUUID();
        this.worker = worker;
    }

    @Override
    public LogicalSide receiver() {
        return LogicalSide.SERVER;
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (!Objects.requireNonNull(context.getSender()).getUUID().equals(player)) {
            return;
        }

        ServerPlayer player = context.getSender();
        player.getCommandSenderWorld().getEntitiesOfClass(AbstractWorkerEntity.class, player.getBoundingBox()
                        .inflate(16.0D), v -> v
                        .getUUID()
                        .equals(worker))
                .stream()
                .filter(AbstractWorkerEntity::isAlive)
                .findAny()
                .ifPresent(worker -> worker.openHireGUI(player));
    }

    public OpenHireGUI fromBytes(FriendlyByteBuf buf) {
        OpenHireGUI msg = new OpenHireGUI();

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
