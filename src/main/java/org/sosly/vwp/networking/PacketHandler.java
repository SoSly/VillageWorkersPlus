package org.sosly.vwp.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.networking.serverbound.OpenHireGUI;
import org.sosly.vwp.networking.clientbound.UpdateHireScreen;
import org.sosly.vwp.networking.serverbound.OpenWorkerGUI;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel network = NetworkRegistry
            .newSimpleChannel(
                new ResourceLocation(VillageWorkersPlus.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
            );

    @SubscribeEvent
    public static void registerMessages(FMLCommonSetupEvent event) {
        int packetId = 0;

        // Register server-bound packets
        registerMessage(packetId++, OpenHireGUI.class);
        registerMessage(packetId++, OpenWorkerGUI.class);
        // Register client-bound packets
        registerMessage(packetId++, UpdateHireScreen.class);
    }

    private static <T extends Message<T>> void registerMessage(int id, Class<T> message) {
        network.registerMessage(id, message,
                Message::toBytes,
                (FriendlyByteBuf buf) -> {
                    try {
                        T msg = message.getDeclaredConstructor().newInstance();
                        return msg.fromBytes(buf);
                    }
                    catch (Exception e) {
                        VillageWorkersPlus.LOGGER.error("Failed to create message instance for {}", message.getSimpleName(), e);
                        throw new RuntimeException(e);
                    }
                },
                (T msg, Supplier<NetworkEvent.Context> contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    if (msg.isValid(context)) {
                        msg.execute(context);
                    }
                });
    }
}
