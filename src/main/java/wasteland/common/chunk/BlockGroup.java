package wasteland.common.chunk;

import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import wasteland.common.ModTags;

public enum BlockGroup {
    GRASSES(ModTags.Blocks.GRASSES),
    FLOWERS(ModTags.Blocks.FLOWERS),
    FUNGI(ModTags.Blocks.FUNGI);

    public static final BlockGroup[] VALUES = values();

    public static final Codec<BlockGroup> CODEC =
            Codec.STRING.xmap(
                    BlockGroup::valueOf,
                    BlockGroup::toString
            );

    private final TagKey<Block> tag;

    BlockGroup(TagKey<Block> tag) {
        this.tag = tag;
    }

    public boolean matches(BlockState state) {
        return state.is(this.tag);
    }

    public Item[] asItems(RegistryAccess registryAccess) {
        Item[] items = new Item[0];
        var blocks = registryAccess.lookupOrThrow(Registries.BLOCK).get(tag);
        if (blocks.isPresent()) {
            items = blocks.get().stream().map(block -> block.get().asItem()).toArray(Item[]::new);
        }
        return items;
    }
}