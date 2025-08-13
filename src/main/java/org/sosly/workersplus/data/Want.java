package org.sosly.workersplus.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class Want implements ItemPredicate {
    private final Predicate<ItemStack> matcher;
    private final int maxToCollect;
    private final Component name;
    
    public Want(ItemStack stack) {
        this.matcher = s -> ItemStack.isSameItem(s, stack);
        this.maxToCollect = stack.getCount();
        this.name = stack.getHoverName();
    }
    
    public Want(Predicate<ItemStack> matcher, int maxToCollect, Component name) {
        this.matcher = matcher;
        this.maxToCollect = maxToCollect;
        this.name = name;
    }
    
    @Override
    public boolean matches(ItemStack stack) {
        return matcher.test(stack);
    }
    
    @Override
    public int getAmount() {
        return maxToCollect;
    }
    
    @Override
    public boolean isSatisfied(ItemStack stack) {
        return matches(stack) && stack.getCount() >= maxToCollect;
    }
    
    @Override
    public Component getName() {
        return name;
    }
}