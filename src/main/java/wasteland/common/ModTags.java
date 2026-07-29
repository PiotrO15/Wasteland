package wasteland.common;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import wasteland.Wasteland;

public class ModTags {
    public static class Biomes {
        public static final TagKey<Biome> TEMPERATE_ECOSYSTEM_ANCHOR = tag("temperate_ecosystem_anchor");
        public static final TagKey<Biome> COLD_ECOSYSTEM_ANCHOR = tag("cold_ecosystem_anchor");
        public static final TagKey<Biome> WARM_ECOSYSTEM_ANCHOR = tag("warm_ecosystem_anchor");
        public static final TagKey<Biome> TROPICAL_ECOSYSTEM_ANCHOR = tag("tropical_ecosystem_anchor");
        public static final TagKey<Biome> MARINE_ECOSYSTEM_ANCHOR = tag("marine_ecosystem_anchor");

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