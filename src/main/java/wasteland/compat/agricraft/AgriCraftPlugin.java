package wasteland.compat.agricraft;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.plant.AgriPlantModifierFactoryRegistry;
import wasteland.common.block.ecostabilizer.Ecosystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgriCraftPlugin {
    public static void init() {
        AgriPlantModifierFactoryRegistry.register("wasteland:ecosystems", (info) -> {
            List<Ecosystem> ecosystems = new ArrayList<>();

            for (String ecosystemId : info.value().split(",")) {
                Ecosystem ecosystem = Ecosystem.fromName(ecosystemId.trim());
                if (ecosystem != null) {
                    ecosystems.add(ecosystem);
                }
            }

            return Optional.of(new EcosystemGrowthCondition.EcosystemPlantModifier(ecosystems));
        });
        AgriApi.getGrowthConditionRegistry().add(new EcosystemGrowthCondition());

        AgriPlantModifierFactoryRegistry.register("wasteland:biome_type", (info) -> Optional.of(new BiomeTypeGrowthCondition.BiomeTypePlantModifier()));
        AgriApi.getGrowthConditionRegistry().add(new BiomeTypeGrowthCondition());
    }
}
