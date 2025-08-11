package org.sosly.workersplus.blocks;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.blocks.workstations.Porter;

import java.util.Set;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, VillageWorkersPlus.MOD_ID);
    
    public static final RegistryObject<Block> PORTER_BLOCK = BLOCKS.register("porter_block",
            Porter::new);

    @SubscribeEvent
    public static void onCreativeModeTabContent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(PORTER_BLOCK.get());
        }
    }
}