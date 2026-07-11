package wasteland.common.block.ecostabilizer.task;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

public class EcosystemTaskRegistry {
    private static final BiMap<ResourceLocation, MapCodec<? extends EcosystemTask>> taskCodecs = HashBiMap.create();

    public static final Codec<EcosystemTask> CODEC = ResourceLocation.CODEC.dispatch(EcosystemTask::id, resourceLocation -> taskCodecs.get(resourceLocation).codec());

    public static <T extends EcosystemTask> void registerTaskType(ResourceLocation id, MapCodec<T> codec) {
        if (taskCodecs.containsKey(id)) {
            throw new IllegalArgumentException("Action with id " + id + " is already registered.");
        }
        taskCodecs.put(id, codec);
    }

    public static void registerTaskTypes() {
        registerTaskType(BlockEcosystemTask.id, BlockEcosystemTask.CODEC);
        registerTaskType(BiomeEcosystemTask.id, BiomeEcosystemTask.CODEC);
    }
}
