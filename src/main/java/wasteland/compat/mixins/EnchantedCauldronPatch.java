// Licensed under the Apache License, Version 2.0
// See the LICENSE-APACHE-2.0 file for details.

// Thanks, @ACCBDD!
package wasteland.compat.mixins;

import net.favouriteless.enchanted.common.blocks.entity.CauldronBlockEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronBlockEntity.class)
public abstract class EnchantedCauldronPatch {
    @Shadow(remap = false)
    private int fluidAmount;

    @Inject(method = "takeContents", at = @At("TAIL"), remap = false)
    public void mixinTakeContents(Player player, CallbackInfo clr) {
        if (fluidAmount < 0)
            setWater(0);
    }

    @Shadow(remap = false)
    public abstract void setWater(int amount);
}
