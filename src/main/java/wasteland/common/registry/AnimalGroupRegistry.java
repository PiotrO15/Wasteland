package wasteland.common.registry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class AnimalGroupRegistry {
    private static final Set<TagKey<EntityType<?>>> animalGroups = new HashSet<>();

    public static void register(TagKey<EntityType<?>> blockGroup) {
        animalGroups.add(blockGroup);
    }

    public static Set<TagKey<EntityType<?>>> values() {
        return animalGroups;
    }

    public static boolean matches(TagKey<EntityType<?>> tag, Entity entity) {
        return entity.getType().is(tag);
    }

//    public static Item[] asItems(TagKey<EntityType<?>> tag, RegistryAccess registryAccess) {
//        Item[] items = new Item[0];
//        var blocks = registryAccess.lookupOrThrow(Registries.ENTITY_TYPE).get(tag);
//        if (blocks.isPresent()) {
//            items = blocks.get().stream().map(block -> block.get().asItem()).toArray(Item[]::new);
//        }
//        return items;
//    }
}
