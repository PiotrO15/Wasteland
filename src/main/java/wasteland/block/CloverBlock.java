package wasteland.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.jetbrains.annotations.NotNull;

public class CloverBlock extends PinkPetalsBlock {
    private static final TagKey<Block> DEPLETED_SOIL = TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "depleted_soil"));

    public CloverBlock(Properties properties) {
        super(properties);
    }

    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource source) {
        if (!level.isAreaLoaded(pos, 3)) {
            return;
        }

        if (level.getMaxLocalRawBrightness(pos) < 9) {
            return;
        }

        if (state.getValue(AMOUNT) < 3) {
            if (level.getBlockState(pos.below()).is(DEPLETED_SOIL)) {
                level.setBlockAndUpdate(pos, state.setValue(AMOUNT, state.getValue(AMOUNT) + 1));
            }
            return;
        }

        if (state.getValue(AMOUNT) == 3 && level.getBlockState(pos.below()).is(DEPLETED_SOIL) && source.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, state.setValue(AMOUNT, state.getValue(AMOUNT) + 1));
            return;
        }

        BlockState defaultBlockState = this.defaultBlockState();

        long worldSeed = level.getSeed();
        RandomSource noiseSource = RandomSource.create(worldSeed);
        PerlinNoise perlinNoise = PerlinNoise.create(noiseSource, -2, 1.0, 0.5);

        for(int i = 0; i < 4; ++i) {
            BlockPos blockpos = pos.offset(source.nextInt(3) - 1, source.nextInt(5) - 3, source.nextInt(3) - 1);

            double value = perlinNoise.getValue(blockpos.getX() * 0.5, blockpos.getY() * 0.5, blockpos.getZ() * 0.5);

            if (value > 0.05) {
                if (level.getBlockState(blockpos).is(Blocks.AIR) && level.getBlockState(blockpos.below()).is(DEPLETED_SOIL)) {
                    Direction randomDir = Direction.Plane.HORIZONTAL.getRandomDirection(source);
                    level.setBlockAndUpdate(blockpos, defaultBlockState.setValue(FACING, randomDir));
                }
            }
        }
    }
}
