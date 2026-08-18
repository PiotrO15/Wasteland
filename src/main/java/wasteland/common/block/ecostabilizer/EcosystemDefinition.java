package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.registry.ModRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record EcosystemDefinition(List<EcosystemStage> stages) {
    public static final Codec<EcosystemDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            EcosystemStage.CODEC.listOf().fieldOf("stages").forGetter(EcosystemDefinition::stages)
    ).apply(i, EcosystemDefinition::new));

    public List<EcosystemTask> tasksForStage(int stage, RegistryAccess registryAccess) {
        for (var s : stages) {
            if (s.stage() == stage) {
                return s.tasks().stream().map(taskId -> registryAccess.registryOrThrow(ModRegistries.ECOSYSTEM_TASK).get(taskId)).toList();
            }
        }
        return List.of();
    }

    public Set<AnimalSpawner> getAnimalSpawners(int stage) {
        Set<AnimalSpawner> animalSpawners = new HashSet<>();
        for (var s : stages) {
            if (s.stage() == stage) break;

            animalSpawners.addAll(s.animalSpawners());
        }
        return animalSpawners;
    }
}
