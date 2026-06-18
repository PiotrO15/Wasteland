package wasteland.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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
}