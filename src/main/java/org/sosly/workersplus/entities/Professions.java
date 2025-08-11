package org.sosly.workersplus.entities;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.blocks.PointsOfInterest;

public class Professions {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, VillageWorkersPlus.MOD_ID);

    public static final RegistryObject<VillagerProfession> PORTER = PROFESSIONS.register("porter", () -> new VillagerProfession("porter",
            poi -> poi.get() == PointsOfInterest.POI_PORTER.get(),
            poi -> poi.get() == PointsOfInterest.POI_PORTER.get(),
            ImmutableSet.of(),
            ImmutableSet.of(),
            SoundEvents.VILLAGER_CELEBRATE));
}
