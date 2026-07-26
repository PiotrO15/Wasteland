package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;
import wasteland.common.block.ecostabilizer.EcosystemDefinition;
import wasteland.common.registry.ModRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EcostabilizerEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");
    private static final ResourceLocation improvedMachineId = new ResourceLocation("wasteland", "improved_ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(machineId) && !event.getMachine().getDefinition().id().equals(improvedMachineId))
            return;

        Ecosystem ecosystem = getOrCreateEcosystem(event.getMachine());

        if (ecosystem == null) {
            return;
        }

        int radius = getRadius(event.getMachine());

        ChunkEventSystem.getInstance().registerListener(event.getMachine(), radius);
        ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), radius, event.getMachine().getLevel());
        ChunkEventSystem.getInstance().computeBiomeStats(event.getMachine().getPos(), radius, event.getMachine().getLevel(), ecosystem.getAnchorTag());

        recalculateStages(event.getMachine().getPos(), event.getMachine(), event.getMachine().getLevel().registryAccess());

        Wasteland.LOGGER.warn("Computed data for machine formed with id {} at {}", event.getMachine().getDefinition().id(), event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onRemove(MachineRemovedEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(machineId) && !event.getMachine().getDefinition().id().equals(improvedMachineId)) {
            return;
        }

        ChunkEventSystem.getInstance().unregisterListener(event.getMachine().getPos());
        Wasteland.LOGGER.warn("Removed machine with id {}", event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onTick(MachineTickEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (event.getMachine().getLevel().getRandom().nextInt(100) != 0)
            return;

        if (!event.getMachine().getDefinition().id().equals(machineId) && !event.getMachine().getDefinition().id().equals(improvedMachineId))
            return;

        int stage = getStage(event.getMachine());

        if (stage > 1) {
            Wasteland.LOGGER.warn("Trying to spawn an animal!");
            spawnFromCachedList((ServerLevel) event.getMachine().getLevel(), ChunkEventSystem.getRandomPos(event.getMachine().getPos(), event.getMachine().getLevel(), getRadius(event.getMachine())), List.of(EntityType.PIG, EntityType.COW), MobCategory.CREATURE);
        }
    }

    @SubscribeEvent
    public static void onRecipeFinish(MachineOnRecipeFinishEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(improvedMachineId)) return;

        switch (event.getRecipe().getId().toString()) {
            case "wasteland:weak_essence":
                if (!applyResolver((ServerLevel) event.getMachine().getLevel(), ChunkEventSystem.getNextSpiralPos(event.getMachine().getPos(), getRadius(event.getMachine())), "recovering"))
                    event.setCanceled(true);
                break;
            case "wasteland:verdant_essence":
                if (!applyResolver((ServerLevel) event.getMachine().getLevel(), ChunkEventSystem.getNextSpiralPos(event.getMachine().getPos(), getRadius(event.getMachine())), "minecraft"))
                    event.setCanceled(true);
                break;
        }
    }

    public static boolean applyResolver(ServerLevel level, BlockPos pos, String namespace) {
        BoundingBox boundingBox = new BoundingBox(pos.getX(), level.getMinBuildHeight(), pos.getZ(), pos.getX() + 3, level.getMaxBuildHeight(), pos.getZ() + 3);
        List<ChunkAccess> chunks = new ArrayList<>();

        for(int z = SectionPos.blockToSectionCoord(boundingBox.minZ()); z <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++z) {
            for(int x = SectionPos.blockToSectionCoord(boundingBox.minX()); x <= SectionPos.blockToSectionCoord(boundingBox.maxX()); ++x) {
                ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        for(ChunkAccess chunk : chunks) {
            BiFunction<ChunkAccess, BoundingBox, BiomeResolver> resolverFactory = (chunkAccess, box) -> makeEcostabilizerResolver(chunkAccess, box, level, namespace, (h) -> true);
            BiomeResolver resolver = resolverFactory.apply(chunk, boundingBox);
            chunk.fillBiomesFromNoise(resolver, level.getChunkSource().randomState().sampler());
            chunk.setUnsaved(true);
        }

        level.getChunkSource().chunkMap.resendBiomesForChunks(chunks);
        return true;
    }

    public static BiomeResolver makeEcostabilizerResolver(ChunkAccess chunk, BoundingBox boundingBox, ServerLevel level, String targetNamespace, Predicate<Holder<Biome>> predicate) {
        return (x, y, z, sampler) -> {
            int quartX = QuartPos.toBlock(x);
            int quartY = QuartPos.toBlock(y);
            int quartZ = QuartPos.toBlock(z);
            Holder<Biome> oldBiomeHolder = chunk.getNoiseBiome(x, y, z);

            if (!boundingBox.isInside(quartX, quartY, quartZ)) {
                return oldBiomeHolder;
            }

            ResourceLocation oldBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(oldBiomeHolder.value());
            if (oldBiome == null) {
                return oldBiomeHolder;
            }

            if (predicate.test(oldBiomeHolder)) {
                level.sendParticles(ParticleTypes.SCRAPE, quartX + 2, quartY + 2, quartZ + 2, 8, 2.0F, 2.0F, 2.0F, 1.0F);
                Optional<Holder.Reference<Biome>> newBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolder(ResourceKey.create(Registries.BIOME, new ResourceLocation(targetNamespace, oldBiome.getPath())));
                if (newBiome.isPresent()) {
                    if (y != 79)
                        return newBiome.get();

                    if (oldBiomeHolder != newBiome.get()) {
                        BlockPos pos = new BlockPos(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
                        ChunkEventSystem.getInstance().notifyBiomeDelta(pos, oldBiomeHolder, newBiome.get(), level);
                        Wasteland.LOGGER.warn("Changed biome at {}", pos);
                    }

                    return newBiome.get();
                }
            }

            return oldBiomeHolder;
        };
    }

    public static Ecosystem getEcosystem(MBDMachine machine) {
        String ecosystemName = machine.getCustomData().getString("ecosystem");

        if (ecosystemName.isEmpty()) {
            return null;
        } else {
            return Ecosystem.fromName(ecosystemName);
        }
    }

    public static Ecosystem getOrCreateEcosystem(MBDMachine machine) {
        String ecosystemName = machine.getCustomData().getString("ecosystem");

        if (ecosystemName.isEmpty()) {
            for (Ecosystem ecosystem : Ecosystem.values()) {
                if (ecosystem.matches(machine.getLevel().getBiome(machine.getPos()))) {
                    setCustomData(machine, compoundTag -> compoundTag.putString("ecosystem", ecosystem.getFriendlyName()));
                    return ecosystem;
                }
            }
        } else {
            return Ecosystem.fromName(ecosystemName);
        }

        return null;
    }

    public static int getRadius(MBDMachine machine) {
        int radius = machine.getCustomData().getInt("radius");

        if (radius == 0) {
            radius = 24;
        }

        return radius;
    }

    public static int getStage(MBDMachine machine) {
        int stage = machine.getCustomData().getInt("transformation_stage");

        if (stage == 0) {
            stage = 1;
        }

        return stage;
    }

    public static void setStage(MBDMachine machine, int stage) {
        int oldStage = getStage(machine);
        if (stage == oldStage)
            return;

        Ecosystem ecosystem = getEcosystem(machine);
        if (ecosystem == null)
            return;

        int oldRadius = getRadius(machine);

        int newRadius = oldRadius;
        newRadius = switch (stage) {
            case 1 -> 24;
            case 2 -> 40;
            case 3 -> 64;
            case 4 -> 80;
            case 5 -> 96;
            default -> newRadius;
        };

        if (oldRadius < newRadius) {
            int finalNewRadius = newRadius;
            setCustomData(machine, compoundTag -> compoundTag.putInt("radius", finalNewRadius));
        } else
            newRadius = oldRadius;

        setCustomData(machine, compoundTag -> compoundTag.putInt("transformation_stage", stage));
        ChunkEventSystem.getInstance().registerListener(machine, newRadius);
        ChunkEventSystem.getInstance().computeStats(machine.getPos(), newRadius, machine.getLevel());
        ChunkEventSystem.getInstance().computeBiomeStats(machine.getPos(), newRadius, machine.getLevel(), ecosystem.getAnchorTag());
    }

    public static void recalculateStages(BlockPos pos, MBDMachine machine, RegistryAccess registryAccess) {
        String ecosystemType = machine.getCustomData().getString("ecosystem");

        if (ecosystemType.isEmpty())
            return;

        Holder<EcosystemDefinition> ecosystem = registryAccess.lookupOrThrow(ModRegistries.ECOSYSTEM)
                .getOrThrow(ResourceKey.create(ModRegistries.ECOSYSTEM, new ResourceLocation(Wasteland.MOD_ID, ecosystemType)));

        int stageBefore = getStage(machine);

        Wasteland.LOGGER.warn("Starting stage recalculation at {}", pos);
        for (int i = 1; i < 5; i++) {
            boolean completed = true;
            for (var task : ecosystem.get().tasksForStage(i)) {
                if (!task.get().optional() && task.get().getProgress(pos) != 1) {
                    completed = false;
                    break;
                }
            }
            if (!completed || i == 4) {
                Wasteland.LOGGER.warn("Finished recalculating stage {} at {}, {}", i, pos, completed);
                setStage(machine, (completed && i == 4) ? 5 : i);
                break;
            }
        }

        if (getStage(machine) != stageBefore) {
            recalculateStages(pos, machine, registryAccess);
        }
    }

    public static void setCustomData(MBDMachine machine, Consumer<CompoundTag> mutator) {
        CompoundTag copy = machine.getCustomData().copy();
        mutator.accept(copy);
        machine.setCustomData(copy);
    }

    public static void spawnFromCachedList(ServerLevel level, BlockPos pos, List<EntityType<?>> cachedSpawnList, MobCategory category) {
        NaturalSpawner.SpawnState spawnState = level.getChunkSource().getLastSpawnState();
        if (spawnState == null || !spawnState.canSpawnForCategory(category, new ChunkPos(pos))) return;

        BlockPos trySpawnPos = atSurface(level, pos);

        RandomSource random = level.getRandom();
        if (cachedSpawnList.isEmpty()) return;

        EntityType<?> type = cachedSpawnList.get(random.nextInt(cachedSpawnList.size()));

        if (!NaturalSpawner.isSpawnPositionOk(
                SpawnPlacements.getPlacementType(type), level, trySpawnPos, type)) {
            Wasteland.LOGGER.warn("Spawn position at {} is not Ok", trySpawnPos);
            return;
        }

        if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, trySpawnPos, random)) {
            Wasteland.LOGGER.warn("Spawn at {} rules check failed", trySpawnPos);
            return;
        }

        type.spawn(level, trySpawnPos, MobSpawnType.NATURAL);
        Wasteland.LOGGER.warn("Spawning at {}", trySpawnPos);
    }

    public static BlockPos atSurface(ServerLevel level, BlockPos pos) {
        int y = level.getChunkAt(pos).getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }
}
