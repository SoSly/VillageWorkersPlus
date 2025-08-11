package org.sosly.workersplus.events;

import com.talhanation.workers.config.WorkersModConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.EntityTypes;
import org.sosly.workersplus.render.human.PorterHumanRenderer;
import org.sosly.workersplus.render.villager.PorterVillagerRenderer;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Client {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (WorkersModConfig.WorkersLookLikeVillagers.get()) {
            event.registerEntityRenderer(EntityTypes.PORTER.get(), PorterVillagerRenderer::new);
        }
        else {
            event.registerEntityRenderer(EntityTypes.PORTER.get(), PorterHumanRenderer::new);
        }
    }
}
