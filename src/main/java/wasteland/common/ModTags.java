package wasteland.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import wasteland.Wasteland;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> GRASSES = tag("grasses");
        public static final TagKey<Block> FLOWERS = tag("flowers");
        public static final TagKey<Block> FUNGI = tag("fungi");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(Wasteland.MOD_ID, name));
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> TEMPERATE_ECOSYSTEM = tag("temperate_ecosystem");
        public static final TagKey<Biome> COLD_ECOSYSTEM = tag("cold_ecosystem");
        public static final TagKey<Biome> WARM_ECOSYSTEM = tag("warm_ecosystem");
        public static final TagKey<Biome> TROPICAL_ECOSYSTEM = tag("tropical_ecosystem");
        public static final TagKey<Biome> MARINE_ECOSYSTEM = tag("marine_ecosystem");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, new ResourceLocation(Wasteland.MOD_ID, name));
        }
    }
}