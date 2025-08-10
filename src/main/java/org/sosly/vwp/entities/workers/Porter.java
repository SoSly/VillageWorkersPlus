package org.sosly.vwp.entities.workers;

import com.talhanation.workers.CommandEvents;
import com.talhanation.workers.Main;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import com.talhanation.workers.inventory.WorkerHireContainer;
import com.talhanation.workers.inventory.WorkerInventoryContainer;
import com.talhanation.workers.network.MessageOpenGuiWorker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.gui.providers.HireProvider;
import org.sosly.vwp.networking.PacketHandler;
import org.sosly.vwp.networking.clientbound.UpdateHireScreen;
import org.sosly.vwp.networking.serverbound.OpenHireGUI;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Porter extends AbstractWorkerEntity {
    
    public Porter(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
        this.setProfessionName("Porter");
        this.cost = 20;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public boolean canWorkWithoutTool() {
        return true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public boolean isRequiredMainTool(ItemStack tool) {
        return false;
    }

    @Override
    public boolean isRequiredSecondTool(ItemStack tool) {
        return false;
    }

    @Override
    public List<Item> inventoryInputHelp() {
        return null;
    }

    @Override
    public boolean hasAMainTool() {
        return false;
    }

    @Override
    public boolean hasASecondTool() {
        return false;
    }

    @Override
    public Predicate<ItemEntity> getAllowedItems() {
        return null;
    }

    @Override
    public void openHireGUI(Player player) {
        this.navigation.stop();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            VillageWorkersPlus.LOGGER.info("Porter hire GUI opened on client side, sending to server.");
            PacketHandler.network.sendToServer(new OpenHireGUI(player, this.getUUID()));
            return;
        }

        VillageWorkersPlus.LOGGER.info("Porter hire GUI opened on server side for player: " + player.getName().getString());
        PacketHandler.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new UpdateHireScreen(CommandEvents.getWorkersCurrency(), this.getWorkerCost()));
        Consumer<FriendlyByteBuf> extraDataWriter = (packetBuffer) -> packetBuffer.writeUUID(this.getUUID());
        NetworkHooks.openScreen(serverPlayer, new HireProvider<Porter>(Porter.this), extraDataWriter);
    }

    @Override
    public void openGUI(Player player) {
        throw new RuntimeException("Porter does not support openGUI method, yet");
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        return null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractWorkerEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }
}
