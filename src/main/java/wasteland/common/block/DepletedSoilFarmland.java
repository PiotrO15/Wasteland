package wasteland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DepletedSoilFarmland extends FarmBlock {
    public DepletedSoilFarmland(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(BlockPlaceContext p_53249_) {
        return !this.defaultBlockState().canSurvive(p_53249_.getLevel(), p_53249_.getClickedPos()) ? ModBlocks.DEPLETED_SOIL.get().defaultBlockState() : super.getStateForPlacement(p_53249_);
    }

    @Override
    public void tick(BlockState p_221134_, @NotNull ServerLevel p_221135_, @NotNull BlockPos p_221136_, @NotNull RandomSource p_221137_) {
        if (!p_221134_.canSurvive(p_221135_, p_221136_)) {
            turnToDepletedSoil(null, p_221134_, p_221135_, p_221136_);
        }

    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource source) {
        int i = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (i > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, i - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToDepletedSoil(null, state, level, pos);
            }
        } else if (i < 7) {
            level.setBlock(pos, state.setValue(MOISTURE, 7), 2);
        }

    }

    private static boolean isNearWater(LevelReader p_53259_, BlockPos p_53260_) {
        BlockState state = p_53259_.getBlockState(p_53260_);
        for(BlockPos blockpos : BlockPos.betweenClosed(p_53260_.offset(-2, 0, -2), p_53260_.offset(2, 1, 2))) {
            if (state.canBeHydrated(p_53259_, p_53260_, p_53259_.getFluidState(blockpos), blockpos)) {
                return true;
            }
        }

        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(p_53259_, p_53260_);
    }

    @Override
    public void fallOn(Level p_153227_, @NotNull BlockState p_153228_, @NotNull BlockPos p_153229_, @NotNull Entity p_153230_, float p_153231_) {
        if (!p_153227_.isClientSide && net.minecraftforge.common.ForgeHooks.onFarmlandTrample(p_153227_, p_153229_, ModBlocks.DEPLETED_SOIL.get().defaultBlockState(), p_153231_, p_153230_)) { // Forge: Move logic to Entity#canTrample
            turnToDepletedSoil(p_153230_, p_153228_, p_153227_, p_153229_);
        }

        p_153230_.causeFallDamage(p_153231_, 1.0F, p_153230_.damageSources().fall());
    }

    private static boolean shouldMaintainFarmland(BlockGetter p_279219_, BlockPos p_279209_) {
        BlockState plant = p_279219_.getBlockState(p_279209_.above());
        BlockState state = p_279219_.getBlockState(p_279209_);
        return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(p_279219_, p_279209_, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
    }

    public static void turnToDepletedSoil(@Nullable Entity p_270981_, BlockState p_270402_, Level p_270568_, BlockPos p_270551_) {
        BlockState blockstate = pushEntitiesUp(p_270402_, ModBlocks.DEPLETED_SOIL.get().defaultBlockState(), p_270568_, p_270551_);
        p_270568_.setBlockAndUpdate(p_270551_, blockstate);
        p_270568_.gameEvent(GameEvent.BLOCK_CHANGE, p_270551_, GameEvent.Context.of(p_270981_, blockstate));
    }
}
