package wasteland.compat.agricraft;

import com.agricraft.agricraft.api.crop.AgriCrop;
import com.agricraft.agricraft.api.plant.AgriPlant;
import com.agricraft.agricraft.api.plant.IAgriPlantModifier;
import com.agricraft.agricraft.api.requirement.AgriGrowthConditionRegistry;
import com.agricraft.agricraft.api.requirement.AgriGrowthResponse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class BiomeTypeGrowthCondition extends AgriGrowthConditionRegistry.BaseGrowthCondition<ResourceLocation> {
    public record BiomeTypePlantModifier() implements IAgriPlantModifier {}

    public BiomeTypeGrowthCondition() {
        super("biome_type", null, null);
    }

    @Override
    public AgriGrowthResponse check(AgriCrop crop, Level level, BlockPos pos, int strength) {
        Optional<IAgriPlantModifier> ecosystemModifier = crop.getPlant().getModifiers().filter((m) -> m.getClass() == BiomeTypePlantModifier.class).findFirst();
        if (ecosystemModifier.isPresent()) {
            String namespace = level.getBiome(pos).unwrapKey().orElseThrow().location().getNamespace();

            return ((namespace.equals("recovering") || namespace.equals("minecraft")) && level.dimension().location().equals(new ResourceLocation("minecraft:overworld"))) ? AgriGrowthResponse.FERTILE : AgriGrowthResponse.INFERTILE;
        } else {
            return AgriGrowthResponse.FERTILE;
        }
    }

    @Override
    public AgriGrowthResponse apply(AgriPlant plant, int strength, ResourceLocation value) {
        return AgriGrowthResponse.FERTILE;
    }
}
