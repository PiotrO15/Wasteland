package wasteland.common.block.ecostabilizer;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import wasteland.common.ModTags;

public enum Ecosystem {
    COLD(ModTags.Biomes.COLD_ECOSYSTEM_ANCHOR, ModTags.Biomes.COLD_ECOSYSTEM, "cold"),
    MARINE(ModTags.Biomes.MARINE_ECOSYSTEM_ANCHOR, ModTags.Biomes.MARINE_ECOSYSTEM, "marine"),
    TEMPERATE(ModTags.Biomes.TEMPERATE_ECOSYSTEM_ANCHOR, ModTags.Biomes.TEMPERATE_ECOSYSTEM, "temperate"),
    TROPICAL(ModTags.Biomes.TROPICAL_ECOSYSTEM_ANCHOR, ModTags.Biomes.TROPICAL_ECOSYSTEM,  "tropical"),
    WARM(ModTags.Biomes.WARM_ECOSYSTEM_ANCHOR, ModTags.Biomes.WARM_ECOSYSTEM, "warm");

    private final TagKey<Biome> anchorTag;
    private final TagKey<Biome> biomeTag;
    private final String friendlyName;

    Ecosystem(TagKey<Biome> anchorTag, TagKey<Biome> biomeTag, String friendlyName) {
        this.anchorTag = anchorTag;
        this.biomeTag = biomeTag;
        this.friendlyName = friendlyName;
    }

    public TagKey<Biome> getAnchorTag() {
        return anchorTag;
    }

    public TagKey<Biome> getBiomeTag() {
        return biomeTag;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public boolean matches(Holder<Biome> biome) {
        return biome.is(this.anchorTag);
    }

    public static Ecosystem fromName(String string) {
        for (Ecosystem ecosystem : Ecosystem.values()) {
            if (ecosystem.getFriendlyName().equals(string)) {
                return ecosystem;
            }
        }
        return null;
    }
}
