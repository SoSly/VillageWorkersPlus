package org.sosly.workersplus.entities;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.workers.Porter;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, VillageWorkersPlus.MOD_ID);
    
    public static final RegistryObject<EntityType<Porter>> PORTER = ENTITY_TYPES.register("porter",
            () -> EntityType.Builder.of(Porter::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .canSpawnFarFromPlayer()
                    .setTrackingRange(32)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(new ResourceLocation(VillageWorkersPlus.MOD_ID, "porter").toString()));
    
    private static final Map<VillagerProfession, EntityType<? extends AbstractWorkerEntity>> PROFESSION_TO_WORKER = new HashMap<>();
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PROFESSION_TO_WORKER.put(Professions.PORTER.get(), PORTER.get());
        });
    }
    
    public static EntityType<? extends AbstractWorkerEntity> getWorkerForProfession(VillagerProfession profession) {
        return PROFESSION_TO_WORKER.get(profession);
    }
}