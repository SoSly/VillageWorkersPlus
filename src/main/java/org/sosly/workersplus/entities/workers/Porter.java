package org.sosly.workersplus.entities.workers;

import com.talhanation.workers.CommandEvents;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.config.CommonConfig;
import org.sosly.workersplus.data.WorkerRelationships;
import org.sosly.workersplus.entities.ai.*;
import org.sosly.workersplus.gui.providers.HireProvider;
import org.sosly.workersplus.gui.providers.WorkerProvider;
import org.sosly.workersplus.networking.PacketHandler;
import org.sosly.workersplus.networking.clientbound.UpdateHireScreen;
import org.sosly.workersplus.networking.serverbound.OpenHireGUI;
import org.sosly.workersplus.networking.serverbound.OpenWorkerGUI;
import org.sosly.workersplus.tasks.DeliveryTask;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Porter extends AbstractWorkerEntity {
    private WorkerRelationships relationships;
    private DeliveryTask delivery;

    public Porter(EntityType<? extends AbstractWorkerEntity> entityType, Level world) {
        super(entityType, world);
        this.setProfessionName("Porter");
        this.cost = CommonConfig.porterCost;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        CompoundTag relationships = this.relationships.save();
        nbt.put("relationships", relationships);

        CompoundTag deliveryData = this.delivery.save();
        nbt.put("delivery", deliveryData);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        if (nbt.contains("relationships")) {
            CompoundTag relationships = nbt.getCompound("relationships");
            this.relationships.load(relationships);
        }

        if (nbt.contains("delivery")) {
            CompoundTag deliveryData = nbt.getCompound("delivery");
            this.delivery.load(deliveryData);
        }
    }

    @Override
    public boolean canWorkWithoutTool() {
        return true;
    }

    protected void registerDependencies() {
        if (delivery == null) {
            delivery = new DeliveryTask(this);
        }

        if (relationships == null) {
            relationships = new WorkerRelationships();
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        registerDependencies();

        // Delivery task goals
        this.goalSelector.addGoal(3, new PutItemsInContainerGoal(this, delivery));
        this.goalSelector.addGoal(4, new GetItemsFromContainerGoal(this, delivery));
        this.goalSelector.addGoal(5, new MoveToDestinationGoal(this, delivery, 2.5D));
        this.goalSelector.addGoal(6, new AssessWorkerNeedsGoal(this, delivery));
        this.goalSelector.addGoal(7, new TargetKnownWorkerGoal(this, delivery, relationships));
        this.goalSelector.addGoal(8, new EndTaskGoal(delivery));
        this.goalSelector.addGoal(9, new StartTaskGoal(delivery));

        // General worker goals
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3D));
        this.goalSelector.addGoal(12, new ReturnToHomeGoal(this));
        this.goalSelector.addGoal(15, new MeetNewWorkerGoal(this, relationships));
        this.goalSelector.addGoal(20, new RandomStrollGoal(this, 0.65D, 300));
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
            PacketHandler.network.sendToServer(new OpenHireGUI(player, this.getUUID()));
            return;
        }

        PacketHandler.network.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new UpdateHireScreen(CommandEvents.getWorkersCurrency(), this.getWorkerCost()));
        Consumer<FriendlyByteBuf> extraDataWriter = (packetBuffer) -> packetBuffer.writeUUID(this.getUUID());
        NetworkHooks.openScreen(serverPlayer, new HireProvider<Porter>(Porter.this), extraDataWriter);
    }

    @Override
    public void openGUI(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            VillageWorkersPlus.LOGGER.info("Porter inventory GUI opened on client side, sending to server.");
            PacketHandler.network.sendToServer(new OpenWorkerGUI(player, this.getUUID()));
            return;
        }

        Consumer<FriendlyByteBuf> extraDataWriter = (packetBuffer) -> packetBuffer.writeUUID(this.getUUID());
        NetworkHooks.openScreen(serverPlayer, new WorkerProvider<Porter>(Porter.this), extraDataWriter);
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
