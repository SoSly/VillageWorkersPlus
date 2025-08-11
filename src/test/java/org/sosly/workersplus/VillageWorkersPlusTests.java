package org.sosly.workersplus;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(VillageWorkersPlus.MOD_ID)
public class VillageWorkersPlusTests {
    @GameTest(template = "empty", batch = VillageWorkersPlus.MOD_ID)
    public static void modLoadTest(final GameTestHelper test) {
        test.succeedIf(() -> ModList.get().isLoaded(VillageWorkersPlus.MOD_ID));
    }
}
