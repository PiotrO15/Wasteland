package wasteland.common.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import wasteland.Wasteland;
import wasteland.common.registry.BlockGroupRegistry;

import java.util.*;

@AutoRegisterCapability
public class VerdantChunk implements INBTSerializable<CompoundTag> {
    public static final int SUBCHUNK_SIZE = 4;

    private final LevelChunk chunk;

    private final Map<Integer, Map<TagKey<Block>, Integer>> data = new HashMap<>();

    public static final int CURRENT_SCAN_HASH_INDEX = 1;
    private int scannedHash = CURRENT_SCAN_HASH_INDEX;

    public VerdantChunk(LevelChunk chunk) {
        this.chunk = chunk;
    }

    private int subchunkIndex(BlockPos pos) {
        int sx = (pos.getX() & 15) / SUBCHUNK_SIZE;
        int sz = (pos.getZ() & 15) / SUBCHUNK_SIZE;
        return sz * SUBCHUNK_SIZE + sx;
    }

    public void increment(BlockPos pos, TagKey<Block> group) {
        if (chunk.getLevel().isClientSide())
            return;

        data.computeIfAbsent(subchunkIndex(pos), k -> new HashMap<>())
                .merge(group, 1, Integer::sum);
        ChunkEventSystem.getInstance().notifyIncrease(pos, group, 1, chunk.getLevel());
        chunk.setUnsaved(true);
    }

    public void decrement(BlockPos pos, TagKey<Block> group) {
        if (chunk.getLevel().isClientSide())
            return;

        int idx = subchunkIndex(pos);
        Map<TagKey<Block>, Integer> subchunk = data.get(idx);
        if (subchunk == null) return;

        subchunk.compute(group, (k, v) -> (v == null || v <= 1) ? null : v - 1);

        if (subchunk.isEmpty()) data.remove(idx);
        ChunkEventSystem.getInstance().notifyDecrease(pos, group, 1, chunk.getLevel());
        chunk.setUnsaved(true);
    }

    public int getLocalCount(BlockPos pos, TagKey<Block> group) {
        Map<TagKey<Block>, Integer> subchunk = data.get(subchunkIndex(pos));
        return subchunk == null ? 0 : subchunk.getOrDefault(group, 0);
    }

    public void notifyChange(BlockPos pos, BlockState oldState, BlockState newState) {
        if (oldState.getBlock() != newState.getBlock()) {
            for (TagKey<Block> group : BlockGroupRegistry.values()) {
                if (BlockGroupRegistry.matches(group, oldState))
                    decrement(pos, group);
                if (BlockGroupRegistry.matches(group, newState))
                    increment(pos, group);
            }
        }
    }

    public int getTotalCount(TagKey<Block> group) {
        int total = 0;
        for (Map<TagKey<Block>, Integer> subchunk : data.values()) {
            Integer count = subchunk.get(group);
            if (count != null) total += count;
        }
        return total;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.putInt("scannedHash", scannedHash);

        ListTag subchunkList = new ListTag();
        for (Map.Entry<Integer, Map<TagKey<Block>, Integer>> entry : data.entrySet()) {
            CompoundTag subchunkTag = new CompoundTag();
            subchunkTag.putInt("idx", entry.getKey());

            CompoundTag groupsTag = new CompoundTag();
            entry.getValue().forEach((group, count) -> groupsTag.putInt(group.location().toString(), count));

            subchunkTag.put("groups", groupsTag);
            subchunkList.add(subchunkTag);
        }

        root.put("subchunks", subchunkList);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.clear();
        scannedHash = nbt.getInt("scannedHash");

        ListTag subchunkList = nbt.getList("subchunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < subchunkList.size(); i++) {
            CompoundTag subchunkTag = subchunkList.getCompound(i);
            int idx = subchunkTag.getInt("idx");

            CompoundTag groupsTag = subchunkTag.getCompound("groups");
            Map<TagKey<Block>, Integer> groups = new HashMap<>();

            for (TagKey<Block> group : BlockGroupRegistry.values()) {
                if (groupsTag.contains(group.location().toString())) {
                    groups.put(group, groupsTag.getInt(group.location().toString()));
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

    public int scannedHash() {
        return scannedHash;
    }

    private void addSilent(BlockPos pos, TagKey<Block> group) {
        data.computeIfAbsent(subchunkIndex(pos), k -> new HashMap<>())
                .merge(group, 1, Integer::sum);
    }

    public void scanChunk() {
        if (scannedHash == CURRENT_SCAN_HASH_INDEX) return;
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

                    for (TagKey<Block> group : BlockGroupRegistry.values()) {
                        if (BlockGroupRegistry.matches(group, state)) {
                            addSilent(mutable, group);
                        }
                    }
                }
            }
        }

        scannedHash = CURRENT_SCAN_HASH_INDEX;
        chunk.setUnsaved(true);
    }
}
