package wasteland.common.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import wasteland.Wasteland;

import java.util.*;

@AutoRegisterCapability
public class VerdantChunk implements INBTSerializable<CompoundTag> {
    public static final int SUBCHUNK_SIZE = 4;

    private final LevelChunk chunk;

    private final Map<Integer, EnumMap<BlockGroup, Integer>> data = new HashMap<>();

    private boolean scanned = false;

    public VerdantChunk(LevelChunk chunk) {
        this.chunk = chunk;
    }

    private int subchunkIndex(BlockPos pos) {
        int sx = (pos.getX() & 15) / SUBCHUNK_SIZE;
        int sz = (pos.getZ() & 15) / SUBCHUNK_SIZE;
        return sz * SUBCHUNK_SIZE + sx;
    }

    public void increment(BlockPos pos, BlockGroup group) {
        if (chunk.getLevel().isClientSide())
            return;

        data.computeIfAbsent(subchunkIndex(pos), k -> new EnumMap<>(BlockGroup.class))
                .merge(group, 1, Integer::sum);
        ChunkEventSystem.getInstance().notifyIncrease(pos, group, 1, chunk.getLevel());
        chunk.setUnsaved(true);
    }

    public void decrement(BlockPos pos, BlockGroup group) {
        if (chunk.getLevel().isClientSide())
            return;

        int idx = subchunkIndex(pos);
        EnumMap<BlockGroup, Integer> subchunk = data.get(idx);
        if (subchunk == null) return;

        subchunk.compute(group, (k, v) -> (v == null || v <= 1) ? null : v - 1);

        if (subchunk.isEmpty()) data.remove(idx);
        ChunkEventSystem.getInstance().notifyDecrease(pos, group, 1, chunk.getLevel());
        chunk.setUnsaved(true);
    }

    public int getLocalCount(BlockPos pos, BlockGroup group) {
        EnumMap<BlockGroup, Integer> subchunk = data.get(subchunkIndex(pos));
        return subchunk == null ? 0 : subchunk.getOrDefault(group, 0);
    }

    public void notifyChange(BlockPos pos, BlockState oldState, BlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            for (BlockGroup group : BlockGroup.values()) {
                if (group.matches(oldState))
                    decrement(pos, group);
                if (group.matches(newState))
                    increment(pos, group);
            }
        }
    }

    public int getTotalCount(BlockGroup group) {
        int total = 0;
        for (EnumMap<BlockGroup, Integer> subchunk : data.values()) {
            Integer count = subchunk.get(group);
            if (count != null) total += count;
        }
        return total;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.putBoolean("scanned", scanned);

        ListTag subchunkList = new ListTag();
        for (Map.Entry<Integer, EnumMap<BlockGroup, Integer>> entry : data.entrySet()) {
            CompoundTag subchunkTag = new CompoundTag();
            subchunkTag.putInt("idx", entry.getKey());

            CompoundTag groupsTag = new CompoundTag();
            entry.getValue().forEach((group, count) -> groupsTag.putInt(group.name(), count));

            subchunkTag.put("groups", groupsTag);
            subchunkList.add(subchunkTag);
        }

        root.put("subchunks", subchunkList);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.clear();
        scanned = nbt.getBoolean("scanned");

        ListTag subchunkList = nbt.getList("subchunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < subchunkList.size(); i++) {
            CompoundTag subchunkTag = subchunkList.getCompound(i);
            int idx = subchunkTag.getInt("idx");

            CompoundTag groupsTag = subchunkTag.getCompound("groups");
            EnumMap<BlockGroup, Integer> groups = new EnumMap<>(BlockGroup.class);

            for (BlockGroup group : BlockGroup.VALUES) {
                if (groupsTag.contains(group.name())) {
                    groups.put(group, groupsTag.getInt(group.name()));
                }
            }

            if (!groups.isEmpty()) data.put(idx, groups);
        }
    }

    public static VerdantChunk getChunk(BlockPos pos, Level level) {
        return level.getChunkAt(pos)
                .getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException(
                        "VerdantChunk capability missing on chunk at " + pos
                ));
    }

    public boolean isScanned() {
        return scanned;
    }

    private void addSilent(BlockPos pos, BlockGroup group) {
        data.computeIfAbsent(subchunkIndex(pos), k -> new EnumMap<>(BlockGroup.class))
                .merge(group, 1, Integer::sum);
    }

    public void scanChunk() {
        if (scanned) return;
        if (chunk.getLevel().isClientSide()) return;

        data.clear();

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = minY; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    mutable.set(baseX + x, y, baseZ + z);
                    BlockState state = chunk.getBlockState(mutable);

                    for (BlockGroup group : BlockGroup.VALUES) {
                        if (group.matches(state)) {
                            addSilent(mutable, group);
                        }
                    }
                }
            }
        }

        if (!data.isEmpty()) {
            Wasteland.LOGGER.warn("Scanned chunk at {} and found", chunk.getPos());
            data.forEach((k, v) -> {
                v.forEach((k1, v1) -> {
                    Wasteland.LOGGER.warn("Subchunk {}: found {} of {}", k, v1, k1);
                });
            });
        }

        scanned = true;
        chunk.setUnsaved(true);
    }
}
