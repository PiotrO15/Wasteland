package wasteland.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CommonConfig {
    public static final CommonConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.IntValue chunkVersion;
    public final ForgeConfigSpec.ConfigValue<String> overworldDimension;

    private CommonConfig(ForgeConfigSpec.Builder builder) {
        this.chunkVersion = builder.defineInRange("chunk_data_version", 1, 1, Integer.MAX_VALUE);
        this.overworldDimension = builder.define("overworld_id", "minecraft:overworld", CommonConfig::validateDimension);
    }

    static {
        Pair<CommonConfig, ForgeConfigSpec> pair = (new ForgeConfigSpec.Builder()).configure(CommonConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }

    private static boolean validateDimension(final Object obj) {
        return obj instanceof String dimension && ResourceLocation.isValidResourceLocation(dimension);
    }

    public static ResourceLocation getOverworld() {
        return new ResourceLocation(INSTANCE.overworldDimension.get());
    }
}
