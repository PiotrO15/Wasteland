package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public record AnimalSpawner(ResourceKey<EntityType<?>> entityType, CompoundTag nbt) {
    public static final Codec<AnimalSpawner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("id").forGetter(AnimalSpawner::entityType),
            CompoundTag.CODEC.optionalFieldOf("nbt", new CompoundTag()).forGetter(AnimalSpawner::nbt)
    ).apply(instance, AnimalSpawner::new));
}
