package org.sosly.vwp.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.Map;
import java.util.function.Predicate;

public class Need implements ItemPredicate {
    private static final Predicate<ItemStack> FOOD_MATCHER = stack -> stack.isEdible() && stack.getFoodProperties(null) != null;
    private static final Map<Item, Predicate<ItemStack>> TOOL_MATCHERS = Map.of(
            Items.BOW, stack -> stack.getItem() instanceof BowItem,
            Items.CROSSBOW, stack -> stack.getItem() instanceof CrossbowItem,
            Items.FISHING_ROD, stack -> stack.getItem() instanceof FishingRodItem,
            Items.IRON_AXE, stack -> stack.getItem() instanceof AxeItem,
            Items.IRON_HOE, stack -> stack.getItem() instanceof HoeItem,
            Items.IRON_PICKAXE, stack -> stack.getItem() instanceof PickaxeItem,
            Items.IRON_SWORD, stack -> stack.getItem() instanceof SwordItem,
            Items.IRON_SHOVEL, stack -> stack.getItem() instanceof ShovelItem,
            Items.SHEARS, stack -> stack.getItem() instanceof ShearsItem,
            Items.SHIELD, stack -> stack.getItem() instanceof ShieldItem
    );

    private final int amount;

    private final Predicate<ItemStack> matcher;
    private final Component name;

    public Need(Predicate<ItemStack> matcher, int amount, Component name) {
        this.matcher = matcher;
        this.name = name;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isSatisfied(ItemStack stack) {
        return matches(stack) && stack.getCount() >= amount;
    }

    public boolean matches(ItemStack stack) {
        return matcher.test(stack);
    }

    public Component getName() {
        return name;
    }


    public static Need food() {
        // todo: configure food amount in config
        return new Need(FOOD_MATCHER, 16, Component.translatable("vwp.need.food"));
    }

    public static Need item(Item item) {
        Predicate<ItemStack> matcher = TOOL_MATCHERS.get(item);
        if (matcher == null) {
            matcher = stack -> stack.is(item);
        }
        return new Need(matcher, 1, item.getName(new ItemStack(item)));
    }
}
