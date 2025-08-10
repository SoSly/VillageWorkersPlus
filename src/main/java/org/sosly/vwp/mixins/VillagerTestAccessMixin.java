package org.sosly.vwp.mixins;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerTestAccessMixin {
    @Invoker("increaseMerchantCareer")
    void vwp$increaseMerchantCareer();
}