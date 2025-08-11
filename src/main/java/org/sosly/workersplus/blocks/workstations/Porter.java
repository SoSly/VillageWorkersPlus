package org.sosly.workersplus.blocks.workstations;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Porter extends Block {
    
    public Porter() {
        super(BlockBehaviour.Properties.copy(Blocks.FLETCHING_TABLE));
    }
}
