package wasteland.compat.mixins;

import cy.jdkdigital.productivebees.compat.sussy.SussyMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SussyMinecraft.class)
public class ProductiveBeesSussyBeePatch {
    @Inject(method = "getLootTables", at = @At("HEAD"), remap = false, cancellable = true)
    private static void getLootTables(ServerLevel level, BlockPos pos, CallbackInfoReturnable<List<ResourceLocation>> cir) {
        Holder<Biome> biome = level.getBiome(pos);
        List<ResourceLocation> tables = new ArrayList<>();

        TagKey<Biome> WATER_BIOMES = TagKey.create(Registries.BIOME, new ResourceLocation("wasteland", "is_water"));

        if(biome.is(WATER_BIOMES)) {

            if (level.getBlockState(pos).is(Blocks.SUSPICIOUS_SAND)) {
                tables.add(new ResourceLocation("wasteland", "archaeology/water_sand"));
            } else {
                tables.add(new ResourceLocation("wasteland", "archaeology/water_gravel"));
            }
        }
        cir.setReturnValue(tables);
    }
}
