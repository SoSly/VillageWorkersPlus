package org.sosly.vwp.entities.workers;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.jetbrains.annotations.NotNull;
import org.sosly.vwp.VillageWorkersPlus;
import org.sosly.vwp.entities.EntityTypes;
import org.sosly.vwp.items.Items;
import org.sosly.vwp.mixins.VillagerTestAccessMixin;

@PrefixGameTestTemplate(false)
@GameTestHolder(VillageWorkersPlus.MOD_ID)
public class PorterTests {
    private static final String BATCH = "porter_tests";

    @GameTest(template = "empty", batch = BATCH)
    public static void testCartographerSellsPorterTable(final @NotNull GameTestHelper test) {
        Entity cartographer = test.spawnWithNoFreeWill(EntityType.VILLAGER, 2, 2, 2);
        Villager villager = (Villager) cartographer;
        VillagerData data = villager.getVillagerData();
        villager.setVillagerData(data.setProfession(VillagerProfession.CARTOGRAPHER));
        ((VillagerTestAccessMixin) villager).vwp$increaseMerchantCareer();

        test.assertTrue(villager.getVillagerData().getLevel() == 2, "Cartographer did not level up to level 2");

        test.assertTrue(villager.getOffers().stream()
                .anyMatch(offer -> offer.getResult().getItem() == Items.PORTER_BLOCK_ITEM.get()),
                "Cartographer does not sell porter table");
        
        test.succeed();
    }
    
    @GameTest(template = "empty", batch = BATCH)
    public static void testPorterSpawn(final @NotNull GameTestHelper test) {
        Entity porter = test.spawnWithNoFreeWill(EntityTypes.PORTER.get(), 2, 2, 2);
        
        test.assertTrue(porter != null, "Porter entity was not created");
        test.assertTrue(porter instanceof Porter, "Entity is not a Porter instance");
        
        Porter porterEntity = (Porter) porter;
        test.assertTrue(porterEntity.isAlive(), "Porter is not alive");
        test.assertTrue(porterEntity.canWorkWithoutTool(), "Porter should be able to work without tools");

        test.succeed();
    }
}
