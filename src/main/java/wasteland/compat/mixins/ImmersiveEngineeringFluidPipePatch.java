package wasteland.compat.mixins;

import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidPipeBlockEntity.class)
public class ImmersiveEngineeringFluidPipePatch {
    @Shadow
    private byte connections;

    @Inject(method = "getAvailableConnectionByte", at = @At("HEAD"), remap = false, cancellable = true)
    public void guardAgainstNullLevel(CallbackInfoReturnable<Byte> cir) {
        FluidPipeBlockEntity self = (FluidPipeBlockEntity) (Object) this;
        if (self.getLevel() == null) {
            cir.setReturnValue(this.connections);
        }
    }
}
