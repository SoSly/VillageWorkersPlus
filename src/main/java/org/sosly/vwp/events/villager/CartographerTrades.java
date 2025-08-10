package org.sosly.vwp.events.villager;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.items.Items;

import java.util.List;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID)
public class CartographerTrades extends VillageWorkersPlusTrades {
    static Item EMERALD = net.minecraft.world.item.Items.EMERALD;
    
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.CARTOGRAPHER) {
            if (CommonConfig.enablePorters) {
                Trade block = new Trade(EMERALD, 25, Items.PORTER_BLOCK_ITEM.get(), 1, 4, 20);
                List<VillagerTrades.ItemListing> list = event.getTrades().get(2);
                list.add(block);
                event.getTrades().put(2, list);
            }
        }
    }
}
