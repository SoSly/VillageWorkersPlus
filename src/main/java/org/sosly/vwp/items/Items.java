package org.sosly.vwp.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.blocks.Blocks;
import org.sosly.vwp.entities.EntityTypes;

public class Items {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, VillageWorkersPlus.MOD_ID);
    
    public static final RegistryObject<Item> PORTER_BLOCK_ITEM = ITEMS.register("porter_block",
            () -> new BlockItem(Blocks.PORTER_BLOCK.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> PORTER_SPAWN_EGG = ITEMS.register("porter_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityTypes.PORTER, 0x8B4513, 0xFFD700, new Item.Properties()));
}