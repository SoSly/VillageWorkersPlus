package org.sosly.vwp;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Configuration options will go here
    private static final ForgeConfigSpec.BooleanValue ENABLE_COURIERS = BUILDER
            .comment("Enable Courier villagers")
            .define("enableCouriers", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_COOKS = BUILDER
            .comment("Enable Cook villagers")
            .define("enableCooks", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_BANKERS = BUILDER
            .comment("Enable Banker villagers")
            .define("enableBankers", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableCouriers;
    public static boolean enableCooks;
    public static boolean enableBankers;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableCouriers = ENABLE_COURIERS.get();
        enableCooks = ENABLE_COOKS.get();
        enableBankers = ENABLE_BANKERS.get();
    }
}