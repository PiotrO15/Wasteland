package wasteland.client;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class EcostabilizerRadiusMapCache {
    private static Map<BlockPos, Integer> ecostabilizers = new HashMap<>();
    private static int version = 0;

    public static void set(Map<BlockPos, Integer> map) {
        ecostabilizers = Map.copyOf(map);
        version++;
    }

    public static Map<BlockPos, Integer> ecostabilizers() {
        return ecostabilizers;
    }

    public static int version() {
        return version;
    }

    public static void clear() {
        set(Map.of());
    }
}
