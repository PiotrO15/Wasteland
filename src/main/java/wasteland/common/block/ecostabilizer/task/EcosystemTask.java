package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.block.ecostabilizer.AnimalSpawner;

import java.util.List;

public interface EcosystemTask {
    ResourceLocation id();

    int getGoal();

    int getProgressValue(BlockPos pos);

    double getProgress(BlockPos pos);
    boolean optional();

    List<AnimalSpawner> getAnimalSpawners();

    IGuiTexture getIcon(RegistryAccess registryAccess);

    String[] getTooltip();
}
