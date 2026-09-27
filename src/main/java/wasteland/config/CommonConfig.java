package wasteland.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class CommonConfig {
    public static final CommonConfig INSTANCE;
    public static final ForgeConfigSpec SPEC;

    public final ForgeConfigSpec.IntValue chunkVersion;

    private CommonConfig(ForgeConfigSpec.Builder builder) {
        this.chunkVersion = builder.defineInRange("chunk_data_version", 1, 1, Integer.MAX_VALUE);
    }

    static {
        Pair<CommonConfig, ForgeConfigSpec> pair = (new ForgeConfigSpec.Builder()).configure(CommonConfig::new);
        INSTANCE = pair.getLeft();
        SPEC = pair.getRight();
    }
}
