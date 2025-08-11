package org.sosly.workersplus.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.EntityTypes;
import org.sosly.workersplus.entities.workers.Porter;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityTypes.PORTER.get(), Porter.createAttributes().build());
    }
}