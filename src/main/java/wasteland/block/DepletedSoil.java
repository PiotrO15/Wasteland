package wasteland.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DepletedSoil extends Block {
    private final SoilState state;

    private final Map<SoilState, TagKey<Block>> catalysts = Map.of(
            SoilState.DEPLETED, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "depleted_soil_catalyst")),
            SoilState.POOR, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "poor_soil_catalyst")),
            SoilState.RESTORING, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "restoring_soil_catalyst"))
    );

    public DepletedSoil(SoilState state, Properties p_49795_) {
        super(p_49795_);
        this.state = state;
    }

    public @NotNull Optional<BlockState> getNext(@NotNull BlockState state) {
        Block block = state.getBlock();
        if (block.equals(ModBlocks.DEPLETED_SOIL.get())) {
            return Optional.of(ModBlocks.POOR_SOIL.get().defaultBlockState());
        } else if (block.equals(ModBlocks.POOR_SOIL.get())) {
            return Optional.of(ModBlocks.RESTORING_SOIL.get().defaultBlockState());
        } else if (block.equals(ModBlocks.RESTORING_SOIL.get())) {
            return Optional.of(Blocks.DIRT.defaultBlockState());
        }
        return Optional.empty();
    }

    public @NotNull SoilState getAge() {
        return state;
    }

    public boolean isRandomlyTicking(@NotNull BlockState state) {
        return true;
    }

    public void randomTick(@NotNull BlockState blockState, @NotNull ServerLevel level, @NotNull BlockPos blockPos, @NotNull RandomSource randomSource) {
        if (randomSource.nextFloat() < 0.11377778F) {
            this.applyChangeOverTime(blockState, level, blockPos, randomSource);
        }
    }

    public void applyChangeOverTime(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource source) {
        int i = this.getAge().ordinal();
        int j = 0;
        int k = 0;

        BlockState nextState = getNext(blockState).isPresent() ? getNext(blockState).get() : null;

        if (nextState == null) {
            return;
        }

        BlockState stateAbove = level.getBlockState(blockPos.above());
        if (stateAbove.is(nextState.getBlock())) {
            level.setBlockAndUpdate(blockPos, nextState);
            return;
        }

        TagKey<Block> catalystTag = null;
        if (blockState.getBlock() instanceof DepletedSoil ds) {
            catalystTag = catalysts.get(ds.getAge());
        }

//         for(BlockPos blockpos : BlockPos.withinManhattan(blockPos.above(), 4, 4, 4)) {
        for(BlockPos blockpos : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
//            int l = blockpos.distManhattan(blockPos.above());
//            if (l > 4) {
//                break;
//            }

            if (!blockpos.equals(blockPos)) {
                BlockState blockstate = level.getBlockState(blockpos);

                if (catalystTag != null && blockstate.is(catalystTag)) {
                    level.setBlockAndUpdate(blockPos, nextState);
                }
            }
        }

//        float f = (float)(k + 1) / (float)(k + j + 1);
//        float f1 = f * f;
//        if (p_220956_.nextFloat() < f1) {
//            this.getNext(blockState).ifPresent((p_153039_) -> {
//                level.setBlockAndUpdate(blockPos, p_153039_);
//            });
//        }
    }

    public enum SoilState {
        DEPLETED,
        POOR,
        RESTORING
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        Level level = context.getLevel();

        if (toolAction.equals(ToolActions.HOE_TILL) && level.getBlockState(context.getClickedPos().above()).isAir()) {
            if (!level.isClientSide()) {
                List<ItemStack> tillLoot = getTillLoot((ServerLevel) level);
                tillLoot.forEach(itemStack -> popResource(level, context.getClickedPos().above(), itemStack));
            }

            return ModBlocks.DEPLETED_SOIL_FARMLAND.get().defaultBlockState();
        }
        return null;
    }

    public List<ItemStack> getTillLoot(ServerLevel level) {
        ResourceLocation resourcelocation = new ResourceLocation("wasteland", "gameplay/depleted_soil_till");
        if (!resourcelocation.equals(BuiltInLootTables.EMPTY)) {
            LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);

            ServerLevel serverlevel = lootParams.getLevel();
            LootTable lootTable = serverlevel.getServer().getLootData().getLootTable(resourcelocation);
            return lootTable.getRandomItems(lootParams);
        }

        return Collections.emptyList();
    }
}
