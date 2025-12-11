package wasteland.common.block;

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
    private final int fertility = 0;

    private static final Map<SoilState, TagKey<Block>> catalysts = Map.of(
            SoilState.DEPLETED, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "depleted_soil_catalyst")),
            SoilState.POOR, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "poor_soil_catalyst")),
            SoilState.RESTORING, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "restoring_soil_catalyst"))
    );

    private static final Map<SoilState, TagKey<Block>> fertile = Map.of(
            SoilState.DEPLETED, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "fertile_depleted_soil")),
            SoilState.POOR, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "fertile_poor_soil")),
            SoilState.RESTORING, TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "fertile_restoring_soil"))
    );

    private static final TagKey<Block> soilTag = TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "soil"));

    private static final ResourceLocation tillLoot = new ResourceLocation("wasteland", "gameplay/depleted_soil_till");

    public DepletedSoil(SoilState state, Properties properties) {
        super(properties);
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
        // Restore the soil if there's a more fertile block above
        if (level.getBlockState(blockPos.above()).is(soilTag)) {
            BlockState stateAbove = level.getBlockState(blockPos.above());

            if (stateAbove.is(fertile.get(this.getAge()))) {
                this.restore(blockState, level, blockPos);
            }

            return;
        }

        // Check if the nearest blocks are more fertile
        int nearbyBlocks = 0;

        for (BlockPos toCheck : BlockPos.betweenClosed(blockPos.offset(-1, 0, -1), blockPos.offset(1, 0, 1))) {
            if (fertile.get(this.getAge()) != null) {
                BlockState stateAtPos = level.getBlockState(toCheck);
                if (stateAtPos.is(fertile.get(this.getAge()))) {
                    nearbyBlocks++;
                }
            }
        }

        // Force restoration if there are more than 4 more fertile blocks nearby
        if (randomSource.nextFloat() < Math.min(0.1f + (nearbyBlocks * 0.1f), 0.5f)) {
            if (nearbyBlocks > 4) {
                this.restore(blockState, level, blockPos);
            } else {
                this.applyChangeOverTime(blockState, level, blockPos);
            }
        }
    }

    public void applyChangeOverTime(BlockState blockState, ServerLevel level, BlockPos blockPos) {
        TagKey<Block> catalystTag = null;
        if (blockState.getBlock() instanceof DepletedSoil ds) {
            catalystTag = catalysts.get(ds.getAge());
        }

        for(BlockPos blockpos : BlockPos.betweenClosed(blockPos.offset(-4, 0, -4), blockPos.offset(4, 2, 4))) {
            if (!blockpos.equals(blockPos)) {
                BlockState blockstate = level.getBlockState(blockpos);

                if (catalystTag != null && blockstate.is(catalystTag)) {
                    restore(blockState, level, blockPos);
                    return;
                }
            }
        }
    }

    private void restore(BlockState blockState, ServerLevel level, BlockPos blockPos) {
        BlockState nextState = getNext(blockState).isPresent() ? getNext(blockState).get() : null;

        if (nextState == null) {
            return;
        }

        level.setBlockAndUpdate(blockPos, nextState);
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
        if (!tillLoot.equals(BuiltInLootTables.EMPTY)) {
            LootParams lootParams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);

            ServerLevel serverlevel = lootParams.getLevel();
            LootTable lootTable = serverlevel.getServer().getLootData().getLootTable(tillLoot);
            return lootTable.getRandomItems(lootParams);
        }

        return Collections.emptyList();
    }
}
