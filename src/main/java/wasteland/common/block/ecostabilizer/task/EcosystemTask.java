package wasteland.common.block.ecostabilizer.task;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface EcosystemTask {
    ResourceLocation id();

    int getGoal();

    double getProgress(BlockPos pos);

    ResourceLocation getEntry();

    boolean optional();
}
