package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;
import wasteland.common.block.ecostabilizer.task.BiomeEcosystemTask;
import wasteland.common.registry.AnimalGroupRegistry;
import wasteland.common.registry.BlockGroupRegistry;

import java.util.*;
import java.util.function.Consumer;

public class ChunkEventSystem {
    private static final Map<Integer, List<int[]>> SPIRAL_CACHE = new HashMap<>();
    private static final int SAMPLE_AREA = 16;

    private final Map<BlockPos, Map<TagKey<Block>, Integer>> blockData =  new HashMap<>();
    private final Map<BlockPos, EnumMap<BiomeEcosystemTask.BiomeType, Integer>> biomeData = new HashMap<>();
    private final Map<BlockPos, MBDMachine> machineData = new HashMap<>();
    private final Map<BlockPos, Integer> machineRadius = new HashMap<>();
    private final Map<BlockPos, Map<TagKey<EntityType<?>>, Integer>> animalData = new HashMap<>();
    private static final Map<BlockPos, Integer> spiralProgress = new HashMap<>();

    private static ChunkEventSystem instance;

    public static ChunkEventSystem getInstance() {
        if (instance == null) instance = new ChunkEventSystem();
        return instance;
    }

    public void computeStats(BlockPos stabilizer, int radius, Level level) {
        int chunkRadius = radius / 4;
        Map<TagKey<Block>, Integer> groups = new HashMap<>();

        BlockPos quantized = quantize(stabilizer);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                if (Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2)) <= chunkRadius) {
                    mutable.set(quantized.getX() + dx * 4, quantized.getY(), quantized.getZ() + dz * 4);
                    VerdantChunk verdantChunk = level.getChunkAt(mutable)
                            .getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY).resolve().orElse(null);

                    if (verdantChunk != null) {
                        for (TagKey<Block> blockGroup : BlockGroupRegistry.values()) {
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
        blockData.put(stabilizer, groups);
        Wasteland.LOGGER.warn("Computed biodiversity");
    }

    public void computeBiomeStats(BlockPos stabilizer, int radius, Level level, TagKey<Biome> tag) {
        int quartRadius = asQuart(radius);
        EnumMap<BiomeEcosystemTask.BiomeType, Integer> counts = new EnumMap<>(BiomeEcosystemTask.BiomeType.class);

        BlockPos quantized = quantize(stabilizer);
        int buildLimit = level.getMaxBuildHeight();

        for (BiomeEcosystemTask.BiomeType type : BiomeEcosystemTask.BiomeType.values()) {
            Set<String> allowedNamespaces = type.allowedNamespaces();
            int count = 0;

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int dx = -quartRadius; dx <= quartRadius; dx++) {
                for (int dz = -quartRadius; dz <= quartRadius; dz++) {
                    if (dx * dx + dz * dz <= (quartRadius + 0.5) * (quartRadius + 0.5)) {
                        mutable.set(quantized.getX() + dx * 4, buildLimit, quantized.getZ() + dz * 4);

                        int qx = QuartPos.fromBlock(mutable.getX());
                        int qz = QuartPos.fromBlock(mutable.getZ());
                        Holder<Biome> biome = level.getNoiseBiome(qx, 79, qz);
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

    public void computeAnimalStats(BlockPos stabilizer, int radius, Level level) {
        Map<TagKey<EntityType<?>>, Integer> counts = new HashMap<>();

        int quartRadius = asQuart(radius);
        BlockPos quantized = quantize(stabilizer);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();

        Set<UUID> seen = new HashSet<>();

        for (int dx = -quartRadius; dx <= quartRadius; dx++) {
            for (int dz = -quartRadius; dz <= quartRadius; dz++) {
                if (dx * dx + dz * dz <= (quartRadius + 0.5) * (quartRadius + 0.5)) {
                    mutable.set(quantized.getX() + dx * 4, maxBuildHeight, quantized.getZ() + dz * 4);

                    AABB box = new AABB(mutable.getX(), minBuildHeight, mutable.getZ(), mutable.getX() + 4, maxBuildHeight, mutable.getZ() + 4);
                    List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box,
                            mob -> withinRadius(stabilizer, mob.blockPosition(), radius) && !seen.contains(mob.getUUID()));

                    for (Mob mob : mobs) {
                        for (TagKey<EntityType<?>> type : AnimalGroupRegistry.values()) {
                            if (AnimalGroupRegistry.matches(type, mob)) {
                                counts.put(type, counts.getOrDefault(type, 0) + 1);
                            }
                            seen.add(mob.getUUID());
                        }
                    }
                }
            }
        }

        animalData.put(stabilizer, counts);
        counts.forEach((type, count) -> Wasteland.LOGGER.warn("Computed mob stats at {}: {} of {}", stabilizer, count, type));
    }

    public void registerListener(MBDMachine stabilizer, int radius) {
        machineData.put(stabilizer.getPos(), stabilizer);
        machineRadius.put(stabilizer.getPos(), radius);
    }

    public void unregisterListener(BlockPos entity) {
        blockData.remove(entity);
        machineData.remove(entity);
        biomeData.remove(entity);
        machineRadius.remove(entity);
        spiralProgress.remove(entity);
    }

    public void notifyIncrease(BlockPos pos, TagKey<Block> blockGroup, int amount, Level level) {
        notify(pos, entity -> {
            int value = blockData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) + amount;
            blockData.getOrDefault(entity, new HashMap<>()).put(blockGroup, value);
            EcostabilizerEvents.recalculateStages(entity, machineData.get(entity), level.registryAccess());
            Wasteland.LOGGER.warn("Increased multiblock biodiversity at {} with amount {}, now {}", entity, amount, value);
        });
    }

    public void notifyDecrease(BlockPos pos, TagKey<Block> blockGroup, int amount, Level level) {
        notify(pos, entity -> {
            int value = blockData.getOrDefault(entity, Map.of()).getOrDefault(blockGroup, 0) - amount;
            blockData.getOrDefault(entity, new HashMap<>()).put(blockGroup, value);
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

    public int getBiodiversity(BlockPos pos, TagKey<Block> blockGroup) {
        Map<TagKey<Block>, Integer> counts = blockData.get(pos);
        return counts == null ? 0 : counts.getOrDefault(blockGroup, 0);
    }

    public int getAnimalCount(BlockPos pos, TagKey<EntityType<?>> type) {
        Map<TagKey<EntityType<?>>, Integer> counts = animalData.get(pos);
        return counts == null ? 0 : counts.getOrDefault(type, 0);
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
                    EcostabilizerEvents.recalculateStages(machinePos, machineData.get(machinePos), level.registryAccess());
                }
            }
        });
    }

    public Map<BlockPos, Integer> getMachineRadiusMap() {
        return machineRadius;
    }

    private static boolean matches(Holder<Biome> biome, TagKey<Biome> tag, BiomeEcosystemTask.BiomeType type) {
        boolean namespaceMatches = biome.unwrapKey()
                .map(key -> type.allowedNamespaces().contains(key.location().getNamespace()))
                .orElse(false);
        return biome.is(tag) && namespaceMatches;
    }

    public static boolean withinRadius(BlockPos centerPos, BlockPos pos, int radius) {
        BlockPos origin = quantize(centerPos);
        BlockPos qPos = quantize(pos);
        int dx = (qPos.getX() - origin.getX()) / 4;
        int dz = (qPos.getZ() - origin.getZ()) / 4;
        return dx * dx + dz * dz <= (asQuart(radius) + 0.5) * (asQuart(radius) + 0.5);
    }

    public static BlockPos getRandomPos(BlockPos centerPos, Level level, int radius) {
        BlockPos origin = quantize(centerPos);

        while (true) {
            int dx = level.getRandom().nextIntBetweenInclusive(-radius, radius);
            int dz = level.getRandom().nextIntBetweenInclusive(-radius, radius);

            BlockPos random = quantize(new BlockPos(dx + origin.getX(), origin.getY(), dz + origin.getZ()));
            if (withinRadius(random, centerPos, radius)) {
                Wasteland.LOGGER.warn("Found random spot at {}", random);
                return random;
            }
            Wasteland.LOGGER.warn("Failed random spot, not inside the radius {}, {}", radius, random);
        }
    }

    public static BlockPos quantize(BlockPos pos) {
        return new BlockPos(
                QuartPos.toBlock(QuartPos.fromBlock(pos.getX())),
                pos.getY(),
                QuartPos.toBlock(QuartPos.fromBlock(pos.getZ()))
        );
    }

    public static int asQuart(Integer radius) {
        return  radius == null ? 0 : radius / 4;
    }

    private static List<int[]> spiralOffsets(int quartRadius) {
        return SPIRAL_CACHE.computeIfAbsent(quartRadius, r -> {
            List<int[]> offsets = new ArrayList<>();
            int x = 0, z = 0;
            int dx = 0, dz = -1;
            int side = r * 2 + 1;
            int steps = side * side;

            for (int i = 0; i < steps; i++) {
                if (x * x + z * z <= (r + 0.5) * (r + 0.5)) {
                    offsets.add(new int[]{x, z});
                }
                if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                    int temp = dx;
                    dx = -dz;
                    dz = temp;
                }
                x += dx;
                z += dz;
            }
            return offsets;
        });
    }

    public static BlockPos getNextSpiralPos(BlockPos centerPos, int radius) {
        BlockPos origin = quantize(centerPos);
        List<int[]> offsets = spiralOffsets(asQuart(radius));

        int index = spiralProgress.getOrDefault(origin, 0);
        if (index >= offsets.size()) index = 0;

        int[] off = offsets.get(index);
        spiralProgress.put(origin, index + 1);

        return new BlockPos(origin.getX() + off[0] * 4, origin.getY(), origin.getZ() + off[1] * 4);
    }
}
