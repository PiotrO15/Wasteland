package wasteland.client;

import me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import wasteland.common.entity.ModEntityTypes;

public class ClientSetup {
    public static void onModConstruction() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerRenderers);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerEntityRenderers(event);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // drones
        event.registerEntityRenderer(ModEntityTypes.TERRAFORMING_DRONE.get(), RenderDrone::standard);
    }
}