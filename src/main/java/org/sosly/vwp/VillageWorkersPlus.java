package org.sosly.vwp;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosly.vwp.blocks.Blocks;
import org.sosly.vwp.blocks.PointsOfInterest;
import org.sosly.vwp.config.CommonConfig;
import org.sosly.vwp.entities.EntityTypes;
import org.sosly.vwp.entities.Professions;
import org.sosly.vwp.gui.Menus;
import org.sosly.vwp.items.Items;

@Mod(VillageWorkersPlus.MOD_ID)
public class VillageWorkersPlus {
    public static final String MOD_ID = "vwp";
    public static final Logger LOGGER = LogManager.getLogger();

    public VillageWorkersPlus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Blocks.BLOCKS.register(modEventBus);
        EntityTypes.ENTITY_TYPES.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Menus.MENUS.register(modEventBus);
        PointsOfInterest.POIS.register(modEventBus);
        Professions.PROFESSIONS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Village Workers Plus common setup");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Village Workers Plus client setup");
    }
}
