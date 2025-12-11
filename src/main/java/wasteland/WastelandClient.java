package wasteland;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wasteland.common.block.ModBlocks;

@Mod.EventBusSubscriber(modid = Wasteland.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WastelandClient {
    @SubscribeEvent
    public static void registerColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, reader, pos, tintIndex) ->
                reader != null && pos != null ? BiomeColors.getAverageGrassColor(reader, pos) : GrassColor.getDefaultColor(), ModBlocks.CLOVER.get());

        event.register((state, reader, pos, tintIndex) -> {
            if (tintIndex != 0) {
                return reader != null && pos != null ? BiomeColors.getAverageGrassColor(reader, pos) : GrassColor.getDefaultColor();
            } else {
                return -1;
            }
        }, ModBlocks.WILDFLOWERS.get());
    }
}
