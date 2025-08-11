package org.sosly.workersplus.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface ItemPredicate {
    int getAmount();
    boolean isSatisfied(ItemStack stack);
    boolean matches(ItemStack stack);
    Component getName();
}
