package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;
import wasteland.common.block.ecostabilizer.task.BiomeEcosystemTask;

import java.util.*;
import java.util.function.Consumer;

public class ChunkEventSystem {
    private final Map<BlockPos, Map<BlockGroup, Integer>> ecostabilizerData =  new HashMap<>();
    private final Map<BlockPos, EnumMap<BiomeEcosystemTask.BiomeType, Integer>> biomeData = new HashMap<>();
    private final Map<BlockPos, MBDMachine> machineData = new HashMap<>();
    private final Map<BlockPos, Integer> machineRadius = new HashMap<>();

    private static final int SAMPLE_AREA = 16;

    private static ChunkEventSystem instance;

    public static ChunkEventSystem getInstance() {
        if (instance == null) instance = new ChunkEventSystem();
        return instance;
    }

    public void computeStats(BlockPos stabilizer, int radius, Level level) {
        int chunkRadius = radius / 4;
        Map<BlockGroup, Integer> groups = new HashMap<>();

        BlockPos quantized = quantize(stabilizer);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                if (Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)) <= chunkRadius) {
                    mutable.set(quantized.getX() + dx * 4, quantized.getY(), quantized.getZ() + dz * 4);
                    VerdantChunk verdantChunk = level.getChunkAt(mutable)
                            .getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY).resolve().orElse(null);

                    if (verdantChunk != null) {
                        for (BlockGroup blockGroup : BlockGroup.values()) {
                            groups.put(blockGroup, groups.getOrDefault(blockGroup, 0) + verdantChunk.getLocalCount(mutable, blockGroup));
//                            Wasteland.LOGGER.warn("Adding {} {} for subchunk at {}", verdantChunk.getLocalCount(mutable, blockGroup), blockGroup, mutable);
                        }
                    }
                } else {
//                    mutable.set(stabilizer.getX() + dx * 4, stabilizer.getY(), stabilizer.getZ() + dz * 4);
//                    Wasteland.LOGGER.warn("Subchunk at pos {} is outside euclidean radius! {} {}", mutable, dx, dz);
                }
            }
        }
        ecostabilizerData.put(stabilizer, groups);
        Wasteland.LOGGER.warn("Computed biodiversity");
    }

    public void computeBiomeStats(BlockPos stabilizer, int radius, Level level, TagKey<Biome> tag) {
        int chunkRadius = radius / 4;
        EnumMap<BiomeEcosystemTask.BiomeType, Integer> counts = new EnumMap<>(BiomeEcosystemTask.BiomeType.class);

        BlockPos quantized = quantize(stabilizer);

        for (BiomeEcosystemTask.BiomeType type : BiomeEcosystemTask.BiomeType.values()) {
            Set<String> allowedNamespaces = type.allowedNamespaces();
            int count = 0;

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    if (Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)) <= chunkRadius) {
                        mutable.set(quantized.getX() + dx * 4, quantized.getY(), quantized.getZ() + dz * 4);

                        Holder<Biome> biome = level.getBiome(mutable);
                        boolean namespaceMatches = biome.unwrapKey()
                                .map(key -> allowedNamespaces.contains(key.location().getNamespace()))
                                .orElse(false);

                        if (biome.is(tag) && namespaceMatches) {
                            count++;
//                            Wasteland.LOGGER.warn("Biome with type found: pos={} type={}", mutable, type);
                        }
                    }
                }
            }
            counts.put(type, count);
            Wasteland.LOGGER.warn("Computed biome stats at {}, found {} of type {}", mutable, count, type);
        }
        biomeData.put(stabilizer, counts);
    }

    public void registerListener(MBDMachine stabilizer, int radius) {
        machineData.put(stabilizer.getPos(), stabilizer);
        machineRadius.put(stabilizer.getPos(), radius);
    }

    public void unregisterListener(BlockPos entity) {
        ecostabilizerData.remove(entity);
        machineData.remove(entity);
        biomeData.remove(entity);
        machineRadius.remove(entity);
    }

    public void notifyIncrease(BlockPos pos, BlockGroup blockGroup, int amount, Level level) {
        notify(pos, entity -> {
            int value = ecostabilizerData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) + amount;
            ecostabilizerData.getOrDefault(entity, new HashMap<>()).put(blockGroup, value);
            EcostabilizerEvents.recalculateStages(entity, machineData.get(entity), level.registryAccess());
            Wasteland.LOGGER.warn("Increased multiblock biodiversity at {} with amount {}, now {}", entity, amount, value);
        });
    }

    public void notifyDecrease(BlockPos pos, BlockGroup blockGroup, int amount, Level level) {
        notify(pos, entity -> {
            int value = ecostabilizerData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) - amount;
            ecostabilizerData.getOrDefault(entity, new HashMap<>()).put(blockGroup, value);
            EcostabilizerEvents.recalculateStages(entity, machineData.get(entity), level.registryAccess());
            Wasteland.LOGGER.warn("Decreased multiblock biodiversity at {} with amount {}, now {}", entity, amount, value);
        });
    }

    public void notify(BlockPos pos, Consumer<BlockPos> action) {
        for (BlockPos machinePos : new ArrayList<>(machineData.keySet())) {
            Integer radius = machineRadius.get(machinePos);
            if (radius != null && withinRadius(machinePos, pos, radius)) {
                action.accept(machinePos);
            }
        }
    }

    public int getBiodiversity(BlockPos pos, BlockGroup blockGroup) {
        Map<BlockGroup, Integer> counts = ecostabilizerData.get(pos);
        return counts == null ? 0 : counts.getOrDefault(blockGroup, 0);
    }

    public int getBiomeCount(BlockPos stabilizer, BiomeEcosystemTask.BiomeType type) {
        EnumMap<BiomeEcosystemTask.BiomeType, Integer> counts = biomeData.get(stabilizer);
        int samples = counts == null ? 0 : counts.getOrDefault(type, 0);
        return samples * SAMPLE_AREA;
    }

    public void notifyBiomeDelta(BlockPos pos, Holder<Biome> oldBiome, Holder<Biome> newBiome, Level level) {
        notify(pos, machinePos -> {
            MBDMachine machine = machineData.get(machinePos);
            Integer radius = machineRadius.get(machinePos);
            if (machine == null || radius == null) return;

            if (!withinRadius(machinePos, pos, radius)) return;

            Ecosystem ecosystem = EcostabilizerEvents.getEcosystem(machine);
            if (ecosystem == null) return;
            TagKey<Biome> tag = ecosystem.getAnchorTag();

            for (BiomeEcosystemTask.BiomeType type : BiomeEcosystemTask.BiomeType.values()) {
                boolean wasMatch = matches(oldBiome, tag, type);
                boolean isMatch = matches(newBiome, tag, type);

                if (wasMatch != isMatch) {
//                    Wasteland.LOGGER.warn("Biome delta fired: pos={} type={} {}→{}", pos, type, wasMatch, isMatch);
                    EnumMap<BiomeEcosystemTask.BiomeType, Integer> counts =
                            biomeData.computeIfAbsent(machinePos, k -> new EnumMap<>(BiomeEcosystemTask.BiomeType.class));
                    counts.merge(type, isMatch ? 1 : -1, Integer::sum);
                }
            }
        });
    }

    private static boolean matches(Holder<Biome> biome, TagKey<Biome> tag, BiomeEcosystemTask.BiomeType type) {
        boolean namespaceMatches = biome.unwrapKey()
                .map(key -> type.allowedNamespaces().contains(key.location().getNamespace()))
                .orElse(false);
        return biome.is(tag) && namespaceMatches;
    }

    private static boolean withinRadius(BlockPos centerPos, BlockPos pos, int radius) {
        int quartRadius = asQuart(radius);
        BlockPos origin = quantize(centerPos);
        int dx = (pos.getX() - origin.getX()) / 4;
        int dz = (pos.getZ() - origin.getZ()) / 4;
        return dx * dx + dz * dz <= quartRadius * quartRadius;
    }

    private static BlockPos quantize(BlockPos pos) {
        return new BlockPos(
                QuartPos.toBlock(QuartPos.fromBlock(pos.getX())),
                pos.getY(),
                QuartPos.toBlock(QuartPos.fromBlock(pos.getZ()))
        );
    }

    private static int asQuart(Integer radius) {
        return  radius == null ? 0 : radius / 4;
    }
}
