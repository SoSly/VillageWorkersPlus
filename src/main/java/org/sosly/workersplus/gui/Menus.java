package org.sosly.workersplus.gui;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.gui.containers.HireContainer;
import org.sosly.workersplus.gui.containers.WorkerContainer;
import org.sosly.workersplus.gui.screens.HireScreen;
import org.sosly.workersplus.gui.screens.WorkerScreen;

import java.util.UUID;

public class Menus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, VillageWorkersPlus.MOD_ID);

    public static final RegistryObject<MenuType<HireContainer>> HIRE_GUI =
            MENUS.register("hire_container", () -> IForgeMenuType.create((id, inv, data) -> {
                UUID workerID = data.readUUID();

                AbstractWorkerEntity worker = getWorkerByUUID(inv.player, workerID);
                if (worker == null) {
                    return null;
                }

                return new HireContainer(id, inv.player, worker, inv);
            }));

    public static final RegistryObject<MenuType<WorkerContainer>> WORKER_GUI =
            MENUS.register("worker_container", () -> IForgeMenuType.create((id, inv, data) -> {
                UUID workerID = data.readUUID();

                AbstractWorkerEntity worker = getWorkerByUUID(inv.player, workerID);
                if (worker == null) {
                    return null;
                }

                return new WorkerContainer(id, worker, inv);
            }));


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(HIRE_GUI.get(), HireScreen::new);
            MenuScreens.register(WORKER_GUI.get(), WorkerScreen::new);
        });
    }

    private static AbstractWorkerEntity getWorkerByUUID(Player player, UUID uuid) {
        double distance = 10D;
        return player.getCommandSenderWorld().getEntitiesOfClass(AbstractWorkerEntity.class,
                new AABB(player.getX() - distance, player.getY() - distance, player.getZ() - distance,
                        player.getX() + distance, player.getY() + distance, player.getZ() + distance),
                entity -> entity.getUUID().equals(uuid))
                        .stream()
                        .findAny()
                        .orElse(null);
    }
}
