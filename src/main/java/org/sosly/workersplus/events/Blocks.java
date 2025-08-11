package org.sosly.workersplus.events;

import com.talhanation.workers.config.WorkersModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.workersplus.VillageWorkersPlus;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = VillageWorkersPlus.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Blocks {
    @SubscribeEvent
    public static void onProfessionBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getState().getBlock().getDescriptionId().contains(VillageWorkersPlus.MOD_ID)) {
            return;
        }

        if (event.getPlayer() != null && event.getPlayer().isCreative()) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        ServerLevel level = Objects.requireNonNull(event.getLevel().getServer()).overworld();

        if (!WorkersModConfig.ProfessionBlocksDrop.get()) {
            return;
        }

        ItemEntity entity = new ItemEntity(level, pos.getX(), pos.getY() + 0.5, pos.getZ(),
                block.asItem().getDefaultInstance());
        entity.setDeltaMovement(
            level.random.triangle(0.0D, 0.11485000171139836D),
            level.random.triangle(0.2D, 0.11485000171139836D),
            level.random.triangle(0.0D, 0.11485000171139836D)
        );
        level.addFreshEntity(entity);
    }
}
