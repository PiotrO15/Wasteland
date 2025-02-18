package wasteland.item;

import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import java.util.function.Predicate;

public class EssenceItem {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() == ModItems.ESSENCE_ITEM.get()) {
            BlockPos pos = event.getPos();
            Level level = event.getLevel();

            if (level.isClientSide()) {
                Random random = new Random();
                for (int i = 0; i < random.nextInt(3, 5); i++) {
                    level.addParticle(ParticleTypes.SCRAPE, pos.getX() + random.nextDouble(), pos.getY() + 1, pos.getZ() + random.nextDouble(), 1, 1, 1);
                }
                return;
            }

            // Set the biome at the location of use
            BlockPos quantizedPos1 = quantize(new BlockPos(pos.getX() - 2, level.getMinBuildHeight(), pos.getZ() - 2));
            BlockPos quantizedPos2 = quantize(new BlockPos(pos.getX() + 2, level.getMaxBuildHeight(), pos.getZ() + 2));
            BoundingBox boundingBox = BoundingBox.fromCorners(quantizedPos1, quantizedPos2);

            ServerLevel serverLevel = (ServerLevel) level;
            List<ChunkAccess> chunkAccessList = new ArrayList<>();

            for(int k = SectionPos.blockToSectionCoord(boundingBox.minZ()); k <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++k) {
                for(int l = SectionPos.blockToSectionCoord(boundingBox.minX()); l <= SectionPos.blockToSectionCoord(boundingBox.maxX()); ++l) {
                    ChunkAccess chunkAccess = serverLevel.getChunk(l, k, ChunkStatus.FULL, false);
                    if (chunkAccess != null)
                        chunkAccessList.add(chunkAccess);
                }
            }

            for(ChunkAccess chunkAccess : chunkAccessList) {
                ResourceLocation resourceLocation = serverLevel.registryAccess().registryOrThrow(Registries.BIOME).getKey(chunkAccess.getNoiseBiome(0, chunkAccess.getHeight(), 0).get());
                if (resourceLocation != null && resourceLocation.getNamespace().equals("wasteland")) {
                    Holder<Biome> biomeHolder = serverLevel.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation("minecraft", resourceLocation.getPath())));

                    chunkAccess.fillBiomesFromNoise(makeResolver(chunkAccess, boundingBox, biomeHolder, (p_262543_) -> true), serverLevel.getChunkSource().randomState().sampler());
                    chunkAccess.setUnsaved(true);
                }
            }

            serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(chunkAccessList);
        }
    }

    private static int quantize(int pos) {
        return QuartPos.toBlock(QuartPos.fromBlock(pos));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(quantize(blockPos.getX()), quantize(blockPos.getY()), quantize(blockPos.getZ()));
    }

    private static BiomeResolver makeResolver(ChunkAccess chunkAccess, BoundingBox boundingBox, Holder<Biome> p_262705_, Predicate<Holder<Biome>> p_262695_) {
        return (p_262550_, p_262551_, p_262552_, p_262553_) -> {
            int i = QuartPos.toBlock(p_262550_);
            int j = QuartPos.toBlock(p_262551_);
            int k = QuartPos.toBlock(p_262552_);
            Holder<Biome> holder = chunkAccess.getNoiseBiome(p_262550_, p_262551_, p_262552_);
            if (boundingBox.isInside(i, j, k) && p_262695_.test(holder)) {
                return p_262705_;
            } else {
                return holder;
            }
        };
    }
}
