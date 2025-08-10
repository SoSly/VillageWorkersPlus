package org.sosly.vwp.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.blocks.workstations.Porter;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = 
            DeferredRegister.create(ForgeRegistries.BLOCKS, VillageWorkersPlus.MOD_ID);
    
    public static final RegistryObject<Block> PORTER_BLOCK = BLOCKS.register("porter_block",
            Porter::new);
}