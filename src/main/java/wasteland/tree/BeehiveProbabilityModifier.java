package wasteland.tree;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class BeehiveProbabilityModifier {
    private static final float BASE_BEEHIVE_PROBABILITY = 0.1F;
    private static final float PER_FLOWER_BEEHIVE_PROBABILITY = 0.05F;
    private static final float MAX_BEEHIVE_PROBABILITY = 0.75F;
    private static final int FLOWER_RADIUS = 5;
    private static final int MAX_DISTANCE_FROM_SAPLING = 5;
    private static final int NUMBER_OF_BEES_TO_SPAWN = 3;

    /**
     * Listens for growing tree from a sapling. If it is oak, birch, cherry or mangrove, it will spawn a beehive
     * with a certain probability based on the number of unique flowers around the sapling.
     * If the beehive is spawned by Minecraft, it will be removed.
     * @param event the event
     */
    @SubscribeEvent
    public static void onSaplingGrow(SaplingGrowTreeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        RandomSource random = level.getRandom();

        BlockState state = level.getBlockState(pos);

        // only if beenest spawns on the tree type
        if (!state.is(Blocks.OAK_SAPLING) && !state.is(Blocks.BIRCH_SAPLING) && !state.is(Blocks.MANGROVE_PROPAGULE) && !state.is(Blocks.CHERRY_SAPLING))
            return;

        // wait until the tree is spawned
        level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1, () -> {
            removeNaturalBeehive(level, pos);

            int uniquePlants = countUniquePlants(level, pos);
            float probability = Math.min(MAX_BEEHIVE_PROBABILITY, BASE_BEEHIVE_PROBABILITY + uniquePlants* PER_FLOWER_BEEHIVE_PROBABILITY);
            if (random.nextFloat() < probability) {
                spawnBeehive(level, pos);
            }
        }));


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
            position.offset(-FLOWER_RADIUS, -FLOWER_RADIUS, -FLOWER_RADIUS),
            position.offset(FLOWER_RADIUS, FLOWER_RADIUS, FLOWER_RADIUS)
        )
            .filter(blockPos -> !blockPos.equals(position))
            .forEach(blockPos -> {
                BlockState state = level.getBlockState(blockPos);
                Block block = state.getBlock();
                if (block instanceof FlowerBlock)
                    uniquePlants.add(block);
            });

        return uniquePlants.size();
    }

    /**
     * Finds a free position for the beehive next to the tree.
     * Implemented with BFS.
     * @param level level
     * @param initialPosition the position of the sapling
     * @param logBlock the log block of the tree
     * @return an empty position next to the tree log or null if there is no empty position
     */
    private static BlockPos getPositionForBeehive(ServerLevel level, BlockPos initialPosition, Block logBlock) {
        List<BlockPos> possiblePositions = new ArrayList<>();
        Set<BlockPos> closed = new HashSet<>();

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(initialPosition.immutable());
        closed.add(initialPosition.immutable());
        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            closed.add(currentPos);
            addEmptySouthPositionToList(possiblePositions, level, initialPosition, currentPos);
            enqueueNeighborLogs(level, initialPosition, currentPos, logBlock, queue, closed);
        }
        if (possiblePositions.isEmpty())
            return null;

        RandomSource random = level.getRandom();
        int i = random.nextInt(possiblePositions.size());
        return possiblePositions.get(i);
    }

    /**
     * Adds the south position to the list of possible positions if it is empty and not already in the list
     * @param list the list of possible positions
     * @param level level
     * @param initialPosition the position of the sapling
     * @param currentLogPosition the current log position
     */
    private static void addEmptySouthPositionToList(List<BlockPos> list, ServerLevel level, BlockPos initialPosition, BlockPos currentLogPosition) {
        BlockPos south = currentLogPosition.relative(Direction.SOUTH);
        Block block = level.getBlockState(south).getBlock();
        if (initialPosition.distManhattan(south) <= BeehiveProbabilityModifier.MAX_DISTANCE_FROM_SAPLING && block instanceof AirBlock && !list.contains(south))
            list.add(south.immutable());
    }

    /**
     * Enqueues the neighboring logs of the current log position to the queue. Searches in a 3x3x3 cube around the current log position.
     * Possible optimization: use a 3x3x1 plane above current log instead of a cube (depends on the tree type and its generation)
     * @param level level
     * @param initialPos the position of the sapling
     * @param currentPos the current log position
     * @param logBlock the log block of the tree
     * @param queue the queue of positions to check
     * @param closed the set of already checked positions
     */
    private static void enqueueNeighborLogs(ServerLevel level, BlockPos initialPos, BlockPos currentPos, Block logBlock, Queue<BlockPos> queue, Set<BlockPos> closed) {
        BlockPos.betweenClosedStream(currentPos.offset(-1, -1, -1), currentPos.offset(1, 1, 1))
            .filter(blockPos -> blockPos.distManhattan(initialPos) <= BeehiveProbabilityModifier.MAX_DISTANCE_FROM_SAPLING)
            .forEach(blockPos -> {
                BlockState state = level.getBlockState(blockPos);
                Block block2 = state.getBlock();
                if (block2.equals(logBlock) && !closed.contains(blockPos) && !queue.contains(blockPos)) {
                    queue.add(blockPos.immutable());
                }
        });
    }

    /**
     * Spawns a beehive somewhere next to the tree at initial position
     * @param level level
     * @param initialPosition the position of the sapling
     */
    private static void spawnBeehive(ServerLevel level, BlockPos initialPosition) {
        BlockPos position = getPositionForBeehive(level, initialPosition, level.getBlockState(initialPosition).getBlock());
        if (position == null)
            return;

        // set the beehive block state
        BlockState state = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.SOUTH);
        level.setBlock(position, state, 3);

        // spawn bees in the beehive after initialization in the next tick
        level.getServer().tell(new TickTask(level.getServer().getTickCount() + 1, () -> {
            BeehiveBlockEntity blockEntity = (BeehiveBlockEntity) level.getBlockEntity(position);
            if (blockEntity != null)
                for (int i = 0; i < NUMBER_OF_BEES_TO_SPAWN; i++)
                    blockEntity.addOccupant(new Bee(EntityType.BEE, level), false);
        }));
    }

    /**
     * Removes the beehive if it is naturally generated by Minecraft
     * Uses BFS algorithm from initial position
     * @param level level
     * @param initialPosition the position of the sapling
     */
    private static void removeNaturalBeehive(ServerLevel level, BlockPos initialPosition) {
        Set<BlockPos> closed = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        BlockState state = level.getBlockState(initialPosition);
        Block logBlock = state.getBlock();

        queue.add(initialPosition.immutable());
        closed.add(initialPosition.immutable());
        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            closed.add(currentPos.immutable());

            BlockPos.betweenClosedStream(
                currentPos.offset(-1, -1, -1),
                currentPos.offset(1, 1, 1)
            )
                .filter(blockPos -> !blockPos.equals(currentPos))
                .forEach(blockPos -> {
                    BlockState state2 = level.getBlockState(blockPos);
                    Block block2 = state2.getBlock();
                    if (block2 instanceof BeehiveBlock) {
                        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }

                    if (block2.equals(logBlock) && !closed.contains(blockPos))
                        queue.add(blockPos.immutable());
                });
        }
    }
}
