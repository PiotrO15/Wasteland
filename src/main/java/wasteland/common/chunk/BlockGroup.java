package wasteland.common.chunk;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import wasteland.common.ModTags;

import java.util.Optional;

public enum BlockGroup {
    GRASSES(ModTags.Blocks.GRASSES),
    FLOWERS(ModTags.Blocks.FLOWERS),
    FUNGI(ModTags.Blocks.FUNGI);

    public static final BlockGroup[] VALUES = values();

    private final TagKey<Block> tag;

    BlockGroup(TagKey<Block> tag) {
        this.tag = tag;
    }

    public boolean matches(BlockState state) {
        return state.is(this.tag);
    }

    public static Optional<BlockGroup> fromState(BlockState state) {
        for (BlockGroup group : VALUES) {
            if (group.matches(state)) return Optional.of(group);
        }
        return Optional.empty();
    }
}