package wasteland.compat.mixins;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.crop.AgriCrop;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.compat.harvest.HarvestCompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HarvestCompatHandler.class)
public class AgriCraftFarmerBeeCompat {
    @Inject(method = "isCropValid", at = @At("RETURN"), cancellable = true, remap = false)
    private static void injectIsCropValid(ProductiveBee bee, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Optional<AgriCrop> optionalCrop = AgriApi.getCrop(bee.level(), pos);
        if (optionalCrop.isPresent()) {
            AgriCrop crop = optionalCrop.get();
            if (crop.canBeHarvested()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "harvestBlock", at = @At("TAIL"), remap = false)
    private static void injectHarvestBlock(ProductiveBee bee, BlockPos pos, CallbackInfo ci) {
        Optional<AgriCrop> optionalCrop = AgriApi.getCrop(bee.level(), pos);
        if (optionalCrop.isPresent()) {
            AgriCrop crop = optionalCrop.get();
            if (crop.canBeHarvested()) {
                crop.harvest(drop -> spawnEntity(bee.level(), pos, drop), null);
            }
        }
    }

    private static void spawnEntity(Level world, BlockPos pos, ItemStack stack) {
        double x = pos.getX() + 0.5 + 0.25 * world.getRandom().nextDouble();
        double y = pos.getY() + 0.5 + 0.25 * world.getRandom().nextDouble();
        double z = pos.getZ() + 0.5 + 0.25 * world.getRandom().nextDouble();
        world.addFreshEntity(new ItemEntity(world, x, y, z, stack));
    }
}
