package wasteland.compat.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wasteland.common.chunk.VerdantChunk;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    public void onStateChange(BlockPos pos, BlockState state, boolean isMoving, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() == null) return;

        LevelChunk levelChunk = (LevelChunk)(Object) this;
        if (levelChunk.getLevel().isClientSide())
            return;

        BlockState oldState = cir.getReturnValue();
        VerdantChunk.getChunk(pos, levelChunk.getLevel()).notifyChange(pos, oldState, state);
    }
}
