package wasteland.common.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import wasteland.Wasteland;
import wasteland.common.block.EcostabilizerBlockEntity;

import java.util.*;
import java.util.function.Consumer;

public class ChunkEventSystem {
    private final Map<Long, Set<EcostabilizerBlockEntity>> chunkToListeners = new HashMap<>();
    private final Map<EcostabilizerBlockEntity, Set<Long>> listenerToChunks = new HashMap<>();

    private final Map<BlockPos, Integer> ecostabilizerData =  new HashMap<>();

    private static ChunkEventSystem instance;

    public static ChunkEventSystem getInstance() {
        if (instance == null) instance = new ChunkEventSystem();
        return instance;
    }

    public void registerListener(BlockPos stabilizer, int chunkRadius, EcostabilizerBlockEntity entity) {
        ChunkPos center = new ChunkPos(stabilizer);
        Set<Long> keys = new HashSet<>();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                long key = ChunkPos.asLong(center.x + dx, center.z + dz);
                chunkToListeners.computeIfAbsent(key, k -> new HashSet<>()).add(entity);
                keys.add(key);
            }
        }

        listenerToChunks.put(entity, keys);
    }

    public int computeStats(BlockPos stabilizer, int chunkRadius, Level level) {
        int value = 0;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                if (Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)) <= chunkRadius) {
                    BlockPos newPos = new BlockPos(stabilizer.getX() + dx * 4, stabilizer.getY(), stabilizer.getZ() + dz * 4);
                    VerdantChunk verdantChunk = level.getChunkAt(newPos)
                            .getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY).resolve().orElse(null);

                    if (verdantChunk != null) {
                        value += verdantChunk.getLocalCount(newPos, BlockGroup.GRASSES);
                        Wasteland.LOGGER.warn("Adding {} for subchunk at {}", verdantChunk.getLocalCount(newPos, BlockGroup.GRASSES), newPos);
                    }
                } else {
                    BlockPos newPos = new BlockPos(stabilizer.getX() + dx * 4, stabilizer.getY(), stabilizer.getZ() + dz * 4);
                    Wasteland.LOGGER.warn("Subchunk at pos {} is outside euclidean radius! {} {}", newPos, dx, dz);
                }
            }
        }
        ecostabilizerData.put(stabilizer, value);
        return value;
    }

    public void unregisterListener(EcostabilizerBlockEntity entity) {
        Set<Long> keys = listenerToChunks.remove(entity);
        if (keys == null) return;

        for (long key : keys) {
            Set<EcostabilizerBlockEntity> set = chunkToListeners.get(key);
            if (set != null) {
                set.remove(entity);
                if (set.isEmpty()) chunkToListeners.remove(key);
            }
        }
        ecostabilizerData.remove(entity.getBlockPos());
    }

    public void notifyIncrease(BlockPos pos, int amount) {
        notify(new ChunkPos(pos), entity -> entity.increase(amount));
    }

    public void notifyDecrease(BlockPos pos, int amount) {
        notify(new ChunkPos(pos), entity -> entity.decrease(amount));
    }

    private void notify(ChunkPos pos, Consumer<EcostabilizerBlockEntity> action) {
        Set<EcostabilizerBlockEntity> listeners =
                chunkToListeners.get(ChunkPos.asLong(pos.x, pos.z));
        if (listeners == null) return;

        // Snapshot to avoid issues if a listener modifies the set
        new ArrayList<>(listeners).stream()
                .filter(e -> !e.isRemoved())
                .forEach(action);
    }
}
