package wasteland.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class BeehiveProbabilityModifier {
    private static final float PER_FLOWER_BEEHIVE_PROBABILITY = 0.05F;
    private static final float MAX_BEEHIVE_PROBABILITY = 0.4F;
    private static final int HORIZONTAL_FLOWER_RADIUS = 2;
    private static final int VERTICAL_FLOWER_RADIUS = 1;

    /**
     * Listens for growing tree from a sapling. If it is oak, birch, cherry or mangrove, it will spawn a beehive
     * with a certain probability based on the number of unique flowers around the sapling.
     * @param event the event
     */
    @SubscribeEvent
    public static void onSaplingGrow(SaplingGrowTreeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        RandomSource random = level.getRandom();

        BlockState state = level.getBlockState(pos);

        // Handle only oak, birch, cherry and mangrove saplings
        if (!state.is(Blocks.OAK_SAPLING) && !state.is(Blocks.BIRCH_SAPLING) && !state.is(Blocks.MANGROVE_PROPAGULE) && !state.is(Blocks.CHERRY_SAPLING))
            return;

        int uniquePlants = countUniquePlants(level, pos);
        float probability = Math.min(MAX_BEEHIVE_PROBABILITY, uniquePlants * PER_FLOWER_BEEHIVE_PROBABILITY);

        if (random.nextFloat() < probability) {
            if (state.is(Blocks.OAK_SAPLING)) {
                event.setFeature(ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("wasteland", "oak_bees")));
            }
            else if (state.is(Blocks.BIRCH_SAPLING)) {
                event.setFeature(ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("wasteland", "birch_bees")));
            }
            else if (state.is(Blocks.CHERRY_SAPLING)) {
                event.setFeature(ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("wasteland", "cherry_bees")));
            }
            else if (state.is(Blocks.MANGROVE_PROPAGULE)) {
                event.setFeature(ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("wasteland", "mangrove_bees")));
            }
        }
    }

    /**
     * Counts the number of unique flowers around the sapling
     * @param level level
     * @param position sapling position
     * @return the number of unique flowers
     */
    private static int countUniquePlants(ServerLevel level, BlockPos position) {
        Set<Block> uniquePlants = new HashSet<>();

        BlockPos.betweenClosedStream(
            position.offset(-HORIZONTAL_FLOWER_RADIUS, -VERTICAL_FLOWER_RADIUS, -HORIZONTAL_FLOWER_RADIUS),
            position.offset(HORIZONTAL_FLOWER_RADIUS, VERTICAL_FLOWER_RADIUS, HORIZONTAL_FLOWER_RADIUS)
        )
            .filter(blockPos -> !blockPos.equals(position))
            .forEach(blockPos -> {
                BlockState state = level.getBlockState(blockPos);
                Block block = state.getBlock();
                if (state.is(BlockTags.FLOWERS))
                    uniquePlants.add(block);
            });

        return uniquePlants.size();
    }
}
