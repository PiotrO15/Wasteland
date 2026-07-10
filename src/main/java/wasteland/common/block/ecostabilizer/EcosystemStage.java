package wasteland.common.block.ecostabilizer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.registry.ModRegistries;

public record EcosystemStage(int stage, HolderSet<EcosystemTask> tasks) {
    public static final Codec<EcosystemStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("stage").forGetter(ecosystemStage -> ecosystemStage.stage),
            RegistryCodecs.homogeneousList(ModRegistries.ECOSYSTEM_TASK).fieldOf("tasks").forGetter(EcosystemStage::tasks)
    ).apply(instance, EcosystemStage::new));
}
