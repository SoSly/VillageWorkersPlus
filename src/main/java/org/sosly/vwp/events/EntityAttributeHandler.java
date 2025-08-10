package org.sosly.vwp.events;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.entities.EntityTypes;
import org.sosly.vwp.entities.workers.Porter;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeHandler {
    
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityTypes.PORTER.get(), Porter.createAttributes().build());
    }
}