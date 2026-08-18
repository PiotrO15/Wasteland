package wasteland.compat.mixins;

import fr.rakambda.fallingtree.common.tree.builder.TreeBuilder;
import fr.rakambda.fallingtree.common.wrapper.IBlockPos;
import fr.rakambda.fallingtree.common.wrapper.ILevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wasteland.common.block.ModBlocks;

@Mixin(TreeBuilder.class)
public class FallingTreeDeadLogCompat {

    @Inject(method = "getLeavesAround", at = @At("HEAD"), cancellable = true, remap = false)
    public void wasteland$getLeavesAround(ILevel level, IBlockPos blockPos, CallbackInfoReturnable<Long> cir) {
        if (level.getBlockState(blockPos).getBlock().getRaw().equals(ModBlocks.DEAD_LOG.get())) {
            cir.setReturnValue(1L);
        }
    }
}
