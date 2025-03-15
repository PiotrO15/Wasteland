package wasteland.compat;

import com.agricraft.agricraft.common.registry.ModItems;
import me.desht.pneumaticcraft.api.harvesting.HoeHandler;
import me.desht.pneumaticcraft.common.core.ModHarvestHandlers;
import me.desht.pneumaticcraft.common.core.ModHoeHandlers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import wasteland.Wasteland;

@Mod.EventBusSubscriber(modid = Wasteland.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PneumaticCraftPlugin {

    @SubscribeEvent
    public static void registerAgriCraftHarvestHandler(RegisterEvent event) {
        if (ModList.get().isLoaded("pneumaticcraft")) {
            event.register(ModHoeHandlers.HOE_HANDLERS_DEFERRED.getRegistryKey(),
                    helper -> helper.register("wasteland", new HoeHandler(itemStack -> itemStack.is(ModItems.CLIPPER.get()), (i, p) -> {})));

            event.register(ModHarvestHandlers.HARVEST_HANDLERS_DEFERRED.getRegistryKey(),
                    helper -> helper.register("wasteland", new AgriCraftHarvestingDroneHandler())
            );
        }
    }
}
