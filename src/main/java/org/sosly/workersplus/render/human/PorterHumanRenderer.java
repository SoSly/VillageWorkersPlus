package org.sosly.workersplus.render.human;

import com.talhanation.workers.client.render.AbstractWorkersHumanRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.entities.workers.Porter;

public class PorterHumanRenderer extends AbstractWorkersHumanRenderer<Porter> {
    
    private static final ResourceLocation[] TEXTURE = {
            new ResourceLocation(VillageWorkersPlus.MOD_ID, "textures/entity/human/porter.png"),
    };
    
    public PorterHumanRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
    }
    
    @Override
    public ResourceLocation getTextureLocation(Porter entity) {
        return TEXTURE[0];
    }
}
