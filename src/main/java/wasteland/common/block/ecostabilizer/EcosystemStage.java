package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record EcosystemStage(int stage, List<ResourceLocation> tasks, List<AnimalSpawner> animalSpawners) {
    public static final Codec<EcosystemStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("stage").forGetter(ecosystemStage -> ecosystemStage.stage),
            ResourceLocation.CODEC.listOf().fieldOf("tasks").forGetter(EcosystemStage::tasks),
            AnimalSpawner.CODEC.listOf().optionalFieldOf("animal_spawners", List.of()).forGetter(EcosystemStage::animalSpawners)
    ).apply(instance, EcosystemStage::new));
}
