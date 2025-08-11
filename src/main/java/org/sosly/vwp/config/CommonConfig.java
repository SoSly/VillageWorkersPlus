package org.sosly.vwp.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.sosly.vwp.VillageWorkersPlus;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    // General Worker Settings
    static {
        BUILDER.push("general");
        BUILDER.comment("General settings for all workers");
    }
    
    private static final ForgeConfigSpec.BooleanValue WORKERS_ARE_CHATTY = BUILDER
            .comment("Whether workers announce what they're doing")
            .define("workersAreChatty", true);
    
    private static final ForgeConfigSpec.IntValue WORKER_MEETING_DURATION = BUILDER
            .comment("Time in seconds for workers to meet each other")
            .defineInRange("workerMeetingDuration", 5, 2, 30);
    
    private static final ForgeConfigSpec.IntValue WORKER_CHAT_RADIUS = BUILDER
            .comment("How close a worker must be to another to chat (in blocks)")
            .defineInRange("workerChatRadius", 5, 2, 10);
    
    private static final ForgeConfigSpec.IntValue WORKER_DETECTION_RADIUS = BUILDER
            .comment("Radius in blocks that workers will scan for other workers")
            .defineInRange("workerDetectionRadius", 80, 16, 512);
    
    private static final ForgeConfigSpec.IntValue WORKER_CHAT_BROADCAST_RANGE = BUILDER
            .comment("Range in blocks that worker chat messages are visible to players")
            .defineInRange("workerChatBroadcastRange", 30, 10, 100);
    
    private static final ForgeConfigSpec.IntValue WORKER_FOOD_THRESHOLD = BUILDER
            .comment("Minimum food items before a worker needs more")
            .defineInRange("workerFoodThreshold", 3, 1, 20);
    
    private static final ForgeConfigSpec.IntValue WORKER_FOOD_AMOUNT = BUILDER
            .comment("Amount of food items supplied to a worker when they need food")
            .defineInRange("workerFoodAmount", 16, 1, 64);
    
    private static final ForgeConfigSpec.DoubleValue CONTAINER_REACH_DISTANCE = BUILDER
            .comment("Distance in blocks that workers can interact with containers")
            .defineInRange("containerReachDistance", 2.5, 1.0, 5.0);
    
    private static final ForgeConfigSpec.IntValue WORKER_ASSESSMENT_DISTANCE = BUILDER
            .comment("Distance in blocks to assess worker needs")
            .defineInRange("workerAssessmentDistance", 5, 2, 20);
    
    static {
        BUILDER.pop();
    }
    
    // Porter Settings
    static {
        BUILDER.push("porter");
        BUILDER.comment("Porter-specific settings");
    }
    
    private static final ForgeConfigSpec.BooleanValue ENABLE_PORTERS = BUILDER
            .comment("Enable Porter workers (logistics/delivery)")
            .define("enablePorters", true);
    
    static {
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // General Worker Settings
    public static boolean workersAreChatty;
    public static int workerMeetingDuration;
    public static int workerChatRadius;
    public static int workerDetectionRadius;
    public static int workerChatBroadcastRange;
    public static int workerFoodThreshold;
    public static int workerFoodAmount;
    public static double containerReachDistance;
    public static int workerAssessmentDistance;
    
    // Porter Settings
    public static boolean enablePorters;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // General Worker Settings
        workersAreChatty = WORKERS_ARE_CHATTY.get();
        workerMeetingDuration = WORKER_MEETING_DURATION.get();
        workerChatRadius = WORKER_CHAT_RADIUS.get();
        workerDetectionRadius = WORKER_DETECTION_RADIUS.get();
        workerChatBroadcastRange = WORKER_CHAT_BROADCAST_RANGE.get();
        workerFoodThreshold = WORKER_FOOD_THRESHOLD.get();
        workerFoodAmount = WORKER_FOOD_AMOUNT.get();
        containerReachDistance = CONTAINER_REACH_DISTANCE.get();
        workerAssessmentDistance = WORKER_ASSESSMENT_DISTANCE.get();
        
        // Porter Settings
        enablePorters = ENABLE_PORTERS.get();
    }
}