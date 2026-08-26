package wasteland.compat.agricraft;

import com.agricraft.agricraft.api.crop.AgriCrop;
import com.agricraft.agricraft.api.plant.AgriPlant;
import com.agricraft.agricraft.api.plant.IAgriPlantModifier;
import com.agricraft.agricraft.api.requirement.AgriGrowthConditionRegistry;
import com.agricraft.agricraft.api.requirement.AgriGrowthResponse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import wasteland.common.block.ecostabilizer.Ecosystem;

import java.util.List;
import java.util.Optional;

public class EcosystemGrowthCondition extends AgriGrowthConditionRegistry.BaseGrowthCondition<ResourceLocation> {
    public record EcosystemPlantModifier(List<Ecosystem> ecosystems) implements IAgriPlantModifier {}

    public EcosystemGrowthCondition() {
        super("ecosystem", null, null);
    }

    @Override
    public AgriGrowthResponse check(AgriCrop crop, Level level, BlockPos pos, int strength) {
        Optional<IAgriPlantModifier> ecosystemModifier = crop.getPlant().getModifiers().filter((m) -> m.getClass() == EcosystemPlantModifier.class).findFirst();
        if (ecosystemModifier.isPresent()) {
            EcosystemPlantModifier modifier = (EcosystemPlantModifier) ecosystemModifier.get();

            return modifier.ecosystems().stream().anyMatch((e) -> e.matches(level.getBiome(pos))) ? AgriGrowthResponse.FERTILE : AgriGrowthResponse.INFERTILE;
        } else {
            return AgriGrowthResponse.FERTILE;
        }
    }

    @Override
    public AgriGrowthResponse apply(AgriPlant plant, int strength, ResourceLocation value) {
        return AgriGrowthResponse.FERTILE;
    }
}
