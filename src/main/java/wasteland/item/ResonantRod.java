package wasteland.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ResonantRod {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() != ModItems.RESONANT_ROD.get()) {
            return;
        }

        BlockPos pos = event.getPos();
        Level level = event.getLevel();

        int minBuildHeight = level.getMinBuildHeight();

        event.getItemStack().hurtAndBreak(1, event.getEntity(), player -> player.broadcastBreakEvent(event.getHand()));

        while (pos.getY() > minBuildHeight) {
            pos = pos.below();

            if (level.getBlockState(pos).getBlock() == Blocks.AMETHYST_BLOCK) {
                level.playSound(null, event.getPos(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (!level.isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) level;

                    serverLevel.sendParticles(ParticleTypes.NOTE, event.getPos().getX() + 0.5, event.getPos().getY() + 2, event.getPos().getZ() + 0.5, 3, 0.5D, 0.5D, 0.5D, 0.5D);
                }
                return;
            }
        }

        level.playSound(null, event.getPos(), level.getBlockState(event.getPos()).getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
