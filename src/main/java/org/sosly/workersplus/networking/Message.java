package org.sosly.workersplus.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public interface Message<T extends Message<T>> {
    void execute(NetworkEvent.Context context);
    T fromBytes(FriendlyByteBuf bytes);
    void toBytes(FriendlyByteBuf buf);
    LogicalSide receiver();
    default boolean isValid(NetworkEvent.Context context) {
        return context.getDirection()
                .getReceptionSide()
                .equals(receiver());
    }
}
