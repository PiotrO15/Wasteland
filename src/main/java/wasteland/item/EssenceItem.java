package wasteland.item;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EssenceItem {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() == ModItems.ESSENCE_ITEM.get()) {
            BlockPos pos = event.getPos();
            Level level = event.getLevel();

            if (!level.isClientSide()) {
                purifyBiome((ServerLevel) level, pos, event.getEntity());
            } else {
                spawnParticles(level, pos);
            }
        }
    }

    private static int quantize(int pos) {
        return QuartPos.toBlock(QuartPos.fromBlock(pos));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(quantize(blockPos.getX()), quantize(blockPos.getY()), quantize(blockPos.getZ()));
    }

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

    private static void spawnParticles(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            Random random = new Random();
            for (int i = 0; i < random.nextInt(3, 5); i++) {
                level.addParticle(ParticleTypes.SCRAPE, pos.getX() + random.nextDouble(), pos.getY() + 1, pos.getZ() + random.nextDouble(), 1, 1, 1);
            }
        }
    }

    private static void purifyBiome(ServerLevel level, BlockPos pos, Player player) {
        // Prepare the bounding box
        BlockPos corner1 = quantize(new BlockPos(pos.getX() - 4, level.getMinBuildHeight(), pos.getZ() - 4));
        BlockPos corner2 = quantize(new BlockPos(pos.getX() + 4, level.getMaxBuildHeight(), pos.getZ() + 4));
        BoundingBox boundingBox = BoundingBox.fromCorners(corner1, corner2);
        player.sendSystemMessage(Component.literal(boundingBox.toString()));

        // Select all chunks inside the bounding box
        List<ChunkAccess> chunks = new ArrayList<>();
        for (int z = SectionPos.blockToSectionCoord(boundingBox.minZ()); z <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); z++) {
            for (int x = SectionPos.blockToSectionCoord(boundingBox.minX()); x <= SectionPos.blockToSectionCoord(boundingBox.maxX()); x++) {
                ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunks.add(chunk);
                    player.sendSystemMessage(Component.literal("Added chunk at " + x + " " + z));
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
