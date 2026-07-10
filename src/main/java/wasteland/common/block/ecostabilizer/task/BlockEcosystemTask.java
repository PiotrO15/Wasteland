package wasteland.common.block.ecostabilizer.task;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.chunk.BlockGroup;
import wasteland.common.chunk.ChunkEventSystem;

public record BlockEcosystemTask(int goal, ResourceLocation entry, BlockGroup blockGroup, boolean optional) implements EcosystemTask {
    public static final ResourceLocation Id = new ResourceLocation("wasteland", "block_task");

    public static final MapCodec<BlockEcosystemTask> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("goal").forGetter(BlockEcosystemTask::goal),
                    ResourceLocation.CODEC.fieldOf("entry").forGetter(BlockEcosystemTask::entry),
                    BlockGroup.CODEC.fieldOf("block_group").forGetter(BlockEcosystemTask::blockGroup),
                    Codec.BOOL.optionalFieldOf("optional", false).forGetter(BlockEcosystemTask::optional)
            ).apply(instance, BlockEcosystemTask::new)
    );

    @Override
    public ResourceLocation id() {
        return Id;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public double getProgress(BlockPos pos) {
        return Math.min(1, (double) ChunkEventSystem.getInstance().getBiodiversity(pos, blockGroup) / (double) getGoal());
    }

    @Override
    public ResourceLocation getEntry() {
        return entry;
    }
}
