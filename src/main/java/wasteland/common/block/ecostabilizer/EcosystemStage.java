package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.registry.ModRegistries;

import java.util.List;

public record EcosystemStage(int stage, HolderSet<EcosystemTask> tasks, List<AnimalSpawner> animalSpawners) {
    public static final Codec<EcosystemStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("stage").forGetter(ecosystemStage -> ecosystemStage.stage),
            RegistryCodecs.homogeneousList(ModRegistries.ECOSYSTEM_TASK).fieldOf("tasks").forGetter(EcosystemStage::tasks),
            AnimalSpawner.CODEC.listOf().optionalFieldOf("animal_spawners", List.of()).forGetter(EcosystemStage::animalSpawners)
    ).apply(instance, EcosystemStage::new));
}
