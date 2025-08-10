package org.sosly.vwp.render.villager;

import com.talhanation.workers.client.render.AbstractWorkersVillagerRenderer;
import com.talhanation.workers.entities.AbstractInventoryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.sosly.vwp.VillageWorkersPlus;

public class PorterVillagerRenderer extends AbstractWorkersVillagerRenderer {
    
    private static final ResourceLocation[] TEXTURE = {
            new ResourceLocation(VillageWorkersPlus.MOD_ID, "textures/entity/villager/porter.png"),
    };
    
    public PorterVillagerRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
    }
    
    @Override
    public ResourceLocation getTextureLocation(AbstractInventoryEntity entity) {
        return TEXTURE[0];
    }
}
