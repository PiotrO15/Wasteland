package wasteland.compat.mixins;

import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wasteland.client.EcosystemMinimapComponent;

@Mixin(value = FTBChunksClient.class, remap = false)
public class FTBChunksClientMixin {
    @Inject(method = "setupComponents", at = @At("HEAD"))
    private void wasteland$registerComponents(CallbackInfo ci) {
        FTBChunksAPI.clientApi().registerMinimapComponent(new EcosystemMinimapComponent());
    }
}
