package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import wasteland.common.chunk.BlockGroupRegistry;
import wasteland.common.chunk.ChunkEventSystem;

public record BlockEcosystemTask(int goal, TagKey<Block> blockGroup, boolean optional) implements EcosystemTask {
    public static final ResourceLocation id = new ResourceLocation("wasteland", "block_task");

    public BlockEcosystemTask {
        BlockGroupRegistry.register(blockGroup);
    }

    public static final MapCodec<BlockEcosystemTask> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("goal").forGetter(BlockEcosystemTask::goal),
                    TagKey.codec(Registries.BLOCK).fieldOf("block_group").forGetter(BlockEcosystemTask::blockGroup),
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
    public IGuiTexture getIcon(RegistryAccess registryAccess) {
        return new ItemStackTexture(BlockGroupRegistry.asItems(blockGroup, registryAccess));
    }

    @Override
    public String[] getTooltip() {
        return new String[] {"Place at least " + getGoal() + " blocks of given types"};
    }

    public TagKey<Block> getTag() {
        return blockGroup;
    }
}
