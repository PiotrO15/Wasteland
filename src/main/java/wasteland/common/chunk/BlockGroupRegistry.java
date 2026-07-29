package wasteland.common.chunk;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class BlockGroupRegistry {
    private static final Set<TagKey<Block>> blockGroups = new HashSet<>();

    public static void register(TagKey<Block> blockGroup) {
        blockGroups.add(blockGroup);
    }

    public static Set<TagKey<Block>> values() {
        return blockGroups;
    }

    public static boolean matches(TagKey<Block> tag, BlockState state) {
        return state.is(tag);
    }

    public static Item[] asItems(TagKey<Block> tag, RegistryAccess registryAccess) {
        Item[] items = new Item[0];
        var blocks = registryAccess.lookupOrThrow(Registries.BLOCK).get(tag);
        if (blocks.isPresent()) {
            items = blocks.get().stream().map(block -> block.get().asItem()).toArray(Item[]::new);
        }
        return items;
    }
}
