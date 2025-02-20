package wasteland.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class BiomeUtils {
    private static BiomeResolver makePurificationResolver(ChunkAccess chunk, BoundingBox boundingBox, ServerLevel level) {
        return (x, y, z, sampler) -> {
            int quartX = QuartPos.toBlock(x);
            int quartY = QuartPos.toBlock(y);
            int quartZ = QuartPos.toBlock(z);
            Holder<Biome> oldBiomeHolder = chunk.getNoiseBiome(x, y, z);
            if (boundingBox.isInside(quartX, quartY, quartZ)) {
                ResourceLocation oldBiome = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(oldBiomeHolder.get());
                if (oldBiome != null && oldBiome.getNamespace().equals("wasteland")) {
                    return (Holder<Biome>) level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", oldBiome.getPath())));
                }
            }

            return oldBiomeHolder;
        };
    }

    private static int quantize(int pos) {
        return QuartPos.toBlock(QuartPos.fromBlock(pos));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(quantize(blockPos.getX()), quantize(blockPos.getY()), quantize(blockPos.getZ()));
    }

    public static void purifyBiome(ServerLevel level, BlockPos pos, int range) {
        // Prepare the bounding box
        BlockPos corner1 = quantize(new BlockPos(pos.getX() - range, level.getMinBuildHeight(), pos.getZ() - range));
        BlockPos corner2 = quantize(new BlockPos(pos.getX() + range, level.getMaxBuildHeight(), pos.getZ() + range));
        BoundingBox boundingBox = BoundingBox.fromCorners(corner1, corner2);

        // Select all chunks inside the bounding box
        List<ChunkAccess> chunks = new ArrayList<>();
        for (int z = SectionPos.blockToSectionCoord(boundingBox.minZ()); z <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); z++) {
            for (int x = SectionPos.blockToSectionCoord(boundingBox.minX()); x <= SectionPos.blockToSectionCoord(boundingBox.maxX()); x++) {
                ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunks.add(chunk);
                }
            }
        }

        // Apply the purification resolver on all selected chunks
        for (ChunkAccess chunk : chunks) {
            chunk.fillBiomesFromNoise(makePurificationResolver(chunk, boundingBox, level), level.getChunkSource().randomState().sampler());
            chunk.setUnsaved(true);
        }

        level.getChunkSource().chunkMap.resendBiomesForChunks(chunks);
    }
}
