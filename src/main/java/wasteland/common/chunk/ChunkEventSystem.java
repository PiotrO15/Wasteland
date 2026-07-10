package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import wasteland.Wasteland;

import java.util.*;
import java.util.function.Consumer;

public class ChunkEventSystem {
    private final Map<Long, Set<BlockPos>> chunkToListeners = new HashMap<>();
    private final Map<BlockPos, Set<Long>> listenerToChunks = new HashMap<>();

    private final Map<BlockPos, Map<BlockGroup, Integer>> ecostabilizerData =  new HashMap<>();
    private final Map<BlockPos, MBDMachine> machineData = new HashMap<>();

    private static ChunkEventSystem instance;

    public static ChunkEventSystem getInstance() {
        if (instance == null) instance = new ChunkEventSystem();
        return instance;
    }

    public void registerListener(MBDMachine stabilizer, int chunkRadius) {
        ChunkPos center = new ChunkPos(stabilizer.getPos());
        Set<Long> keys = new HashSet<>();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                long key = ChunkPos.asLong(center.x + dx, center.z + dz);
                chunkToListeners.computeIfAbsent(key, k -> new HashSet<>()).add(stabilizer.getPos());
                keys.add(key);
            }
        }

        listenerToChunks.put(stabilizer.getPos(), keys);
        machineData.put(stabilizer.getPos(), stabilizer);
    }

    public void computeStats(BlockPos stabilizer, int chunkRadius, Level level) {
        Map<BlockGroup, Integer> groups = new HashMap<>();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                if (Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)) <= chunkRadius) {
                    BlockPos newPos = new BlockPos(stabilizer.getX() + dx * 4, stabilizer.getY(), stabilizer.getZ() + dz * 4);
                    VerdantChunk verdantChunk = level.getChunkAt(newPos)
                            .getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY).resolve().orElse(null);

                    if (verdantChunk != null) {
                        for (BlockGroup blockGroup : BlockGroup.values()) {
                            groups.put(blockGroup, groups.getOrDefault(blockGroup, 0) + verdantChunk.getLocalCount(newPos, blockGroup));
                            Wasteland.LOGGER.warn("Adding {} {} for subchunk at {}", verdantChunk.getLocalCount(newPos, blockGroup), blockGroup, newPos);
                        }
                    }
                } else {
                    BlockPos newPos = new BlockPos(stabilizer.getX() + dx * 4, stabilizer.getY(), stabilizer.getZ() + dz * 4);
                    Wasteland.LOGGER.warn("Subchunk at pos {} is outside euclidean radius! {} {}", newPos, dx, dz);
                }
            }
        }
        ecostabilizerData.put(stabilizer, groups);
        Wasteland.LOGGER.warn("Computed biodiversity");
    }

    public void unregisterListener(BlockPos entity) {
        Set<Long> keys = listenerToChunks.remove(entity);
        if (keys == null) return;

        for (long key : keys) {
            Set<BlockPos> set = chunkToListeners.get(key);
            if (set != null) {
                set.remove(entity);
                if (set.isEmpty()) chunkToListeners.remove(key);
            }
        }
        ecostabilizerData.remove(entity);
        machineData.remove(entity);
    }

    public void notifyIncrease(BlockPos pos, BlockGroup blockGroup, int amount, Level level) {
        notify(new ChunkPos(pos), entity -> {
            int value = ecostabilizerData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) + amount;
            ecostabilizerData.getOrDefault(entity, Map.of()).put(blockGroup, value);
            EcostabilizerEvents.recalculateStages(entity, machineData.get(entity), level.registryAccess());
            Wasteland.LOGGER.warn("Increased multiblock biodiversity at {} with amount {}, now {}", entity, amount, value);
        });
    }

    public void notifyDecrease(BlockPos pos, BlockGroup blockGroup, int amount, Level level) {
        notify(new ChunkPos(pos), entity -> {
            int value = ecostabilizerData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) - amount;
            ecostabilizerData.getOrDefault(entity, Map.of()).put(blockGroup, value);
            EcostabilizerEvents.recalculateStages(entity, machineData.get(entity), level.registryAccess());
            Wasteland.LOGGER.warn("Decreased multiblock biodiversity at {} with amount {}, now {}", entity, amount, value);
        });
    }

    public int getBiodiversity(BlockPos pos, BlockGroup blockGroup) {
        return ecostabilizerData.getOrDefault(pos, Map.of()).getOrDefault(blockGroup, 0);
    }

    private void notify(ChunkPos pos, Consumer<BlockPos> action) {
        Set<BlockPos> listeners =
                chunkToListeners.get(ChunkPos.asLong(pos.x, pos.z));
        if (listeners == null) return;

        // Snapshot to avoid issues if a listener modifies the set
        new ArrayList<>(listeners).forEach(action);
    }
}
