package org.sosly.workersplus.events;

import com.talhanation.workers.config.WorkersModConfig;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.EntityTypes;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Villagers {
    @SubscribeEvent
    public static void onVillagerLivingUpdate(LivingEvent.LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Villager villager)) {
            return;
        }

        VillagerProfession profession = villager.getVillagerData().getProfession();
        EntityType<? extends AbstractWorkerEntity> workerType = EntityTypes.getWorkerForProfession(profession);
        
        if (workerType == null) {
            return;
        }
        AbstractWorkerEntity worker = workerType.create(entity.level());
        if (worker == null) {
            return;
        }

        worker.copyPosition(entity);
        worker.initSpawn();

        for (int i = 0; i < villager.getInventory().getContainerSize(); i++) {
            worker.getInventory().addItem(villager.getInventory().getItem(i));
        }

        Component name = villager.getCustomName();
        if (name != null) {
            worker.setCustomName(name);
        }

        if (WorkersModConfig.WorkersTablesPOIReleasing.get()) {
            villager.releasePoi(MemoryModuleType.JOB_SITE);
        }
        villager.releasePoi(MemoryModuleType.HOME);
        villager.releasePoi(MemoryModuleType.MEETING_POINT);
        villager.discard();
        villager.getCommandSenderWorld().addFreshEntity(worker);
    }
}
