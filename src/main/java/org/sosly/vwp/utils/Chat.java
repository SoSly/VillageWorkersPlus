package org.sosly.vwp.utils;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.sosly.vwp.config.CommonConfig;

import java.util.List;

public class Chat {
    public static void send(AbstractWorkerEntity sender, Component message) {
        if (sender.level().isClientSide()) {
            return;
        }

        if (message == null || message.getString().isEmpty()) {
            return;
        }

        if (!CommonConfig.workersAreChatty) {
            return;
        }

        List<ServerPlayer> nearbyPlayers = getNearbyPlayers(sender);
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        Component dialogue = sender.getName().plainCopy().append(": ").append(message);;
        for (ServerPlayer player : nearbyPlayers) {
            if (player != null && player.isAlive()) {
                player.sendSystemMessage(dialogue);
            }
        }
    }

    private static List<ServerPlayer> getNearbyPlayers(AbstractWorkerEntity sender) {
        return sender.level().getEntitiesOfClass(
                ServerPlayer.class,
                sender.getBoundingBox().inflate(CommonConfig.workerChatBroadcastRange),
                LivingEntity::isAlive);
    }
}
