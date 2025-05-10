package wasteland.compat.mixins;

import com.agricraft.agricraft.api.crop.AgriCrop;
import com.agricraft.agricraft.api.genetic.AgriCrossBreedEngine;
import com.agricraft.agricraft.api.stat.AgriStat;
import com.agricraft.agricraft.api.stat.AgriStatRegistry;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AgriCrossBreedEngine.class)
public class AgriCraftFertilityPatch {
    @Inject(method = "rollFertility", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectRollFertility(AgriCrop crop, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        AgriStat fertility = AgriStatRegistry.getInstance().fertilityStat();
        double adjustedFertility = 5 + crop.getGenome().getStatGene(fertility).getTrait() / 2.0;
        double randomFertility = 1 + (fertility.getMax() - 1) * random.nextDouble();
        cir.setReturnValue(randomFertility < adjustedFertility);
    }
}
