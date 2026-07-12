package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

public interface EcosystemTask {
    ResourceLocation id();

    int getGoal();

    int getProgressValue(BlockPos pos);

    double getProgress(BlockPos pos);
    boolean optional();

    IGuiTexture getIcon(RegistryAccess registryAccess);

    String[] getTooltip();
}
