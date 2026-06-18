package wasteland.common.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;
import wasteland.Wasteland;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@AutoRegisterCapability
public class VerdantChunk implements INBTSerializable<CompoundTag> {
    public static final int SUBCHUNK_SIZE = 4;

    private final LevelChunk chunk;

    private final Map<Integer, EnumMap<BlockGroup, Integer>> data = new HashMap<>();

    public VerdantChunk(LevelChunk chunk) {
        this.chunk = chunk;
        Wasteland.LOGGER.log(org.apache.logging.log4j.Level.WARN, "Initializing VerdantChunk for chunk at {}", chunk.getPos());
    }

    private int subchunkIndex(BlockPos pos) {
        int sx = (pos.getX() & 15) / SUBCHUNK_SIZE;
        int sz = (pos.getZ() & 15) / SUBCHUNK_SIZE;
        return sz * SUBCHUNK_SIZE + sx;
    }

    public void increment(BlockPos pos, BlockGroup group) {
        data.computeIfAbsent(subchunkIndex(pos), k -> new EnumMap<>(BlockGroup.class))
                .merge(group, 1, Integer::sum);
        chunk.setUnsaved(true);
    }

    public void decrement(BlockPos pos, BlockGroup group) {
        int idx = subchunkIndex(pos);
        EnumMap<BlockGroup, Integer> subchunk = data.get(idx);
        if (subchunk == null) return;

        subchunk.compute(group, (k, v) -> (v == null || v <= 1) ? null : v - 1);

        if (subchunk.isEmpty()) data.remove(idx);
        chunk.setUnsaved(true);
    }

    public int getLocalCount(BlockPos pos, BlockGroup group) {
        EnumMap<BlockGroup, Integer> subchunk = data.get(subchunkIndex(pos));
        return subchunk == null ? 0 : subchunk.getOrDefault(group, 0);
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
}
