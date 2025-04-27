package wasteland.compat.mixins;

import com.agricraft.agricraft.api.codecs.AgriProduct;
import com.agricraft.agricraft.api.crop.AgriGrowthStage;
import com.agricraft.agricraft.api.plant.AgriPlant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AgriPlant.class)
public class AgriPlantAllowsClippingPatch {
    @Shadow @Final private List<AgriProduct> clipProducts;

    @Inject(method = "allowsClipping", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectAllowsClipping(AgriGrowthStage growthStage, ItemStack clipper, @Nullable LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(growthStage.isMature() && !this.clipProducts.isEmpty());
    }
}
