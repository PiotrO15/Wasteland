package wasteland.compat.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wasteland.common.block.ecostabilizer.AnimalEvents;
import wasteland.common.chunk.ChunkEventSystem;

@Mixin(Entity.class)
public class EntityMoveMixin {
    @Unique
    private BlockPos wasteland$lastQuantized = null;

    @Inject(method = "setPos(DDD)V", at = @At("TAIL"))
    private void wasteland$onSetPos(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()) return;
        if (!self.isAddedToWorld()) return;
        if (self.isRemoved()) return;
        if (!(self instanceof Mob mob)) return;

        BlockPos quantized = ChunkEventSystem.quantize(self.blockPosition());
        if (quantized.equals(this.wasteland$lastQuantized)) return;
        this.wasteland$lastQuantized = quantized;

        AnimalEvents.getInstance().onPositionChanged(mob);
    }

    @Inject(method = "moveTo(DDDFF)V", at = @At("TAIL"))
    private void wasteland$onMoveTo(double x, double y, double z, float pitch, float yaw, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()) return;
        if (!self.isAddedToWorld()) return;
        if (self.isRemoved()) return;
        if (!(self instanceof Mob mob)) return;

        BlockPos quantized = ChunkEventSystem.quantize(self.blockPosition());
        if (quantized.equals(this.wasteland$lastQuantized)) return;
        this.wasteland$lastQuantized = quantized;

        AnimalEvents.getInstance().onPositionChanged(mob);
    }
}
