package wasteland;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import wasteland.common.block.ModBlocks;

@EventBusSubscriber(modid = Wasteland.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WastelandClient {
    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, reader, pos, tintIndex) ->
                reader != null && pos != null ? BiomeColors.getAverageGrassColor(reader, pos) : GrassColor.getDefaultColor(), ModBlocks.CLOVER.get());
    }
}
