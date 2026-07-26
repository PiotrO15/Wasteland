package wasteland.compat.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import piotro15.biomeblends.util.BlendBiomeResolver;
import wasteland.common.chunk.ChunkEventSystem;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(BlendBiomeResolver.class)
public class BlendBiomeResolverMixin {

    @Inject(method = "makeNamespaceResolver", at = @At("RETURN"), remap = false, cancellable = true)
    private static void wrapNamespaceResolver(
            ChunkAccess chunk, BoundingBox boundingBox, ServerLevel level,
            String targetNamespace, Optional<ResourceLocation> fallbackBiome,
            Predicate<Holder<Biome>> predicate, ParticleOptions particleOptions,
            CallbackInfoReturnable<BiomeResolver> cir) {

        BiomeResolver original = cir.getReturnValue();
        cir.setReturnValue(wrap(original, chunk, level));
    }

    @Inject(method = "makeResolver", at = @At("RETURN"), remap = false, cancellable = true)
    private static void wrapResolver(
            ChunkAccess chunk, BoundingBox boundingBox, ServerLevel level,
            ResourceLocation targetBiome, Predicate<Holder<Biome>> predicate,
            ParticleOptions particleOptions,
            CallbackInfoReturnable<BiomeResolver> cir) {

        BiomeResolver original = cir.getReturnValue();
        cir.setReturnValue(wrap(original, chunk, level));
    }

    private static BiomeResolver wrap(BiomeResolver original, ChunkAccess chunk, ServerLevel level) {
        return (x, y, z, sampler) -> {
            Holder<Biome> oldBiome = chunk.getNoiseBiome(x, y, z);
            Holder<Biome> newBiome = original.getNoiseBiome(x, y, z, sampler);

            if (y != 79)
                return newBiome;

            if (oldBiome != newBiome) {
                BlockPos pos = new BlockPos(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
                ChunkEventSystem.getInstance().notifyBiomeDelta(pos, oldBiome, newBiome, level);
            }

            return newBiome;
        };
    }
}