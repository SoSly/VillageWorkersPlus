package org.sosly.vwp.blocks;


import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.vwp.VillageWorkersPlus;

import java.util.Set;

public class PointsOfInterest {
    public static final DeferredRegister<PoiType> POIS =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, VillageWorkersPlus.MOD_ID);

    public static final RegistryObject<PoiType> POI_PORTER = POIS.register("porter", () -> {
        Set<BlockState> blockStates = ImmutableSet.copyOf(Blocks.PORTER_BLOCK.get().getStateDefinition().getPossibleStates());
        return new PoiType(blockStates, 1, 1);
    });
}