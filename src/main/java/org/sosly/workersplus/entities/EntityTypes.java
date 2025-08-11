package org.sosly.workersplus.entities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.workers.Porter;

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
}