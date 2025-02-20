package wasteland.item;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.utils.BiomeUtils;

public class EssenceItem {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() == ModItems.ESSENCE_ITEM.get()) {
            BlockPos pos = event.getPos();
            Level level = event.getLevel();

            if (!level.isClientSide()) {
                BiomeUtils.purifyBiome((ServerLevel) level, pos, 4);
                spawnParticles(level, pos);
            }
        }
    }

    private static void spawnParticles(Level level, BlockPos pos) {
        ((ServerLevel) level).sendParticles(ParticleTypes.SCRAPE, pos.getX(), pos.getY(), pos.getZ(), 100, 4, 4, 4, 1);
    }
}
