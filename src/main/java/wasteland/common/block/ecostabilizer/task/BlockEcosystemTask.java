package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.chunk.BlockGroup;
import wasteland.common.chunk.ChunkEventSystem;

public record BlockEcosystemTask(int goal, ResourceLocation entry, BlockGroup blockGroup, boolean optional) implements EcosystemTask {
    public static final ResourceLocation id = new ResourceLocation("wasteland", "block_task");

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
        return id;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public int getProgressValue(BlockPos pos) {
        return ChunkEventSystem.getInstance().getBiodiversity(pos, blockGroup);
    }

    @Override
    public double getProgress(BlockPos pos) {
        return Math.min(1, (double) getProgressValue(pos) / getGoal());
    }

    @Override
    public ResourceLocation getEntry() {
        return entry;
    }

    @Override
    public IGuiTexture getIcon(RegistryAccess registryAccess) {
        return new ItemStackTexture(blockGroup.asItems(registryAccess));
    }

    @Override
    public String[] getTooltip() {
        return new String[0];
    }
}
