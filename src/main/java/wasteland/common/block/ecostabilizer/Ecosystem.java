package wasteland.common.block.ecostabilizer;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import wasteland.common.ModTags;

public enum Ecosystem {
    COLD(ModTags.Biomes.COLD_ECOSYSTEM_ANCHOR, ModTags.Biomes.COLD_ECOSYSTEM, "cold", ModTags.Blocks.COLD_SAPLINGS),
    MARINE(ModTags.Biomes.MARINE_ECOSYSTEM_ANCHOR, ModTags.Biomes.MARINE_ECOSYSTEM, "marine", ModTags.Blocks.MARINE_SAPLINGS),
    TEMPERATE(ModTags.Biomes.TEMPERATE_ECOSYSTEM_ANCHOR, ModTags.Biomes.TEMPERATE_ECOSYSTEM, "temperate", ModTags.Blocks.TEMPERATE_SAPLINGS),
    TROPICAL(ModTags.Biomes.TROPICAL_ECOSYSTEM_ANCHOR, ModTags.Biomes.TROPICAL_ECOSYSTEM,  "tropical", ModTags.Blocks.TROPICAL_SAPLINGS),
    WARM(ModTags.Biomes.WARM_ECOSYSTEM_ANCHOR, ModTags.Biomes.WARM_ECOSYSTEM, "warm", ModTags.Blocks.WARM_SAPLINGS);

    private final TagKey<Biome> anchorTag;
    private final TagKey<Biome> biomeTag;
    private final String friendlyName;
    private final TagKey<Block> saplingTag;

    Ecosystem(TagKey<Biome> anchorTag, TagKey<Biome> biomeTag, String friendlyName, TagKey<Block> saplingTag) {
        this.anchorTag = anchorTag;
        this.biomeTag = biomeTag;
        this.friendlyName = friendlyName;
        this.saplingTag = saplingTag;
    }

    public TagKey<Biome> getAnchorTag() {
        return anchorTag;
    }

    public TagKey<Biome> getBiomeTag() {
        return biomeTag;
    }

    public TagKey<Block> getSaplingTag() {
        return saplingTag;
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

    public static Ecosystem fromBiome(Holder<Biome> biome) {
        for (Ecosystem ecosystem : Ecosystem.values()) {
            if (biome.is(ecosystem.getBiomeTag())) {
                return ecosystem;
            }
        }
        return null;
    }

    public static Ecosystem fromAnchorBiome(Holder<Biome> biome) {
        for (Ecosystem ecosystem : Ecosystem.values()) {
            if (biome.is(ecosystem.getAnchorTag())) {
                return ecosystem;
            }
        }
        return null;
    }
}
