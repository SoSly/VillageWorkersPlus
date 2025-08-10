package org.sosly.vwp.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.sosly.vwp.VillageWorkersPlus;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue ENABLE_PORTERS = BUILDER
            .comment("Enable Porter workers (logistics/delivery)")
            .define("enablePorters", true);
    private static final ForgeConfigSpec.IntValue PORTER_HIRE_COST = BUILDER
            .comment("The amount of currency required to hire a Porter (takes effect after restart)")
            .defineInRange("porterHireCost", 25, 0, 999);

    private static final ForgeConfigSpec.IntValue PORTER_MAX_MEMORY = BUILDER
            .comment("Maximum number of workers a fully-equipped Porter can remember (with Book & Quill, Feather, and Ink Sac). Minimum is 4.")
            .defineInRange("porterMaxMemory", 16, 4, 100);

    private static final ForgeConfigSpec.IntValue PORTER_SCAN_RADIUS = BUILDER
            .comment("Radius in blocks that a Porter will scan for new workers to meet")
            .defineInRange("porterScanRadius", 80, 16, 512);

    private static final ForgeConfigSpec.IntValue PORTER_CHAT_DISTANCE = BUILDER
            .comment("How close a Porter must be to a worker to chat with them (in blocks)")
            .defineInRange("porterChatDistance", 5, 2, 10);

    private static final ForgeConfigSpec.IntValue PORTER_INTRODUCTION_TIME = BUILDER
            .comment("Time in seconds for a Porter to introduce themselves to a new worker")
            .defineInRange("porterIntroductionTime", 30, 5, 120);

    private static final ForgeConfigSpec.IntValue PORTER_VISIT_INTERVAL = BUILDER
            .comment("Time in minutes between Porter visits to each known worker")
            .defineInRange("porterVisitInterval", 5, 1, 60);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enablePorters;
    public static int porterHireCost;
    public static int porterMaxMemory;
    public static int porterScanRadius;
    public static int porterChatDistance;
    public static int porterIntroductionTime;
    public static int porterVisitInterval;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enablePorters = ENABLE_PORTERS.get();
        porterHireCost = PORTER_HIRE_COST.get();
        porterMaxMemory = PORTER_MAX_MEMORY.get();
        porterScanRadius = PORTER_SCAN_RADIUS.get();
        porterChatDistance = PORTER_CHAT_DISTANCE.get();
        porterIntroductionTime = PORTER_INTRODUCTION_TIME.get();
        porterVisitInterval = PORTER_VISIT_INTERVAL.get();
    }

    public static int getPorterMemoryCapacity(boolean hasBook, boolean hasFeather, boolean hasInk) {
        if (!hasBook) {
            return Math.max(1, porterMaxMemory / 4); // Base: 1/4 of max
        }
        if (!hasInk) {
            return Math.max(1, porterMaxMemory / 2); // Book only: 1/2 of max
        }
        if (!hasFeather) {
            return Math.max(1, (porterMaxMemory * 3) / 4); // Book + Ink: 3/4 of max
        }
        return porterMaxMemory; // Full kit: max capacity
    }
}
