package org.sosly.vwp.events.villager;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

abstract class VillageWorkersPlusTrades {
    protected static class Trade implements VillagerTrades.ItemListing {
        private final Item buyingItem;
        private final Item sellingItem;
        private final int buyingAmount;
        private final int sellingAmount;
        private final int maxUses;
        private final int givenExp;
        private final float priceMultiplier;

        public Trade(ItemLike buyingItem, int buyingAmount, ItemLike sellingItem, int sellingAmount, int maxUses,
                     int givenExp) {
            this.buyingItem = buyingItem.asItem();
            this.buyingAmount = buyingAmount;
            this.sellingItem = sellingItem.asItem();
            this.sellingAmount = sellingAmount;
            this.maxUses = maxUses;
            this.givenExp = givenExp;
            this.priceMultiplier = 0.05F;
        }

        public MerchantOffer getOffer(Entity entity, RandomSource random) {
            return new MerchantOffer(new ItemStack(this.buyingItem, this.buyingAmount),
                    new ItemStack(sellingItem, sellingAmount), maxUses, givenExp, priceMultiplier);
        }
    }
}
