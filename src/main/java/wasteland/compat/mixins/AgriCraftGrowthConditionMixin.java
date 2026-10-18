package wasteland.compat.mixins;

import com.agricraft.agricraft.api.requirement.AgriGrowthConditionRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiFunction;

@Mixin(AgriGrowthConditionRegistry.class)
public class AgriCraftGrowthConditionMixin {
    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/agricraft/agricraft/api/requirement/AgriGrowthConditionRegistry$BaseGrowthCondition;<init>(Ljava/lang/String;Lcom/agricraft/agricraft/api/requirement/AgriGrowthConditionRegistry$ResponseGetter;Ljava/util/function/BiFunction;)V", ordinal = 3), index = 2, remap = false)
    private BiFunction<LevelReader, BlockPos, Integer> agri$useVanillaCropLight(BiFunction<LevelReader, BlockPos, Integer> original) {
        return (level, pos) -> level.getRawBrightness(pos, 0);
    }
}
