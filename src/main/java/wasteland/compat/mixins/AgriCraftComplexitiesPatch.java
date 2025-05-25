package wasteland.compat.mixins;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.codecs.AgriMutation;
import com.agricraft.agricraft.api.genetic.AgriMutationHandler;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AgriMutationHandler.class)
public class AgriCraftComplexitiesPatch {
    @Shadow
    private boolean computed = false;

    @Inject(method = "setupComplexities", at = @At("HEAD"), remap = false)
    private void injectElseForEmptyRegistry(CallbackInfo ci) {
        Optional<Registry<AgriMutation>> op = AgriApi.getMutationRegistry();
        if (op.isEmpty()) {
            this.computed = false;
        }
    }
}
