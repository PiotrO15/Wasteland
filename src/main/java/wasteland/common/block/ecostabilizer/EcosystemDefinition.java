package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;

import java.util.List;

public record EcosystemDefinition(ResourceLocation id, List<EcosystemStage> stages) {
    public static final Codec<EcosystemDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("ecosystem").forGetter(EcosystemDefinition::id),
            EcosystemStage.CODEC.listOf().fieldOf("stages").forGetter(EcosystemDefinition::stages)
    ).apply(i, EcosystemDefinition::new));

    public HolderSet<EcosystemTask> tasksForStage(int stage) {
        for (var s : stages) {
            if (s.stage() == stage) return s.tasks();
        }
        return HolderSet.direct();
    }
}
