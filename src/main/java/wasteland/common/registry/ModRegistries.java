package wasteland.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DataPackRegistryEvent;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.EcosystemDefinition;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.block.ecostabilizer.task.EcosystemTaskRegistry;

public class ModRegistries {
    public static final ResourceKey<Registry<EcosystemTask>> ECOSYSTEM_TASK =
            ResourceKey.createRegistryKey(new ResourceLocation(Wasteland.MOD_ID, "ecosystem_task"));

    public static final ResourceKey<Registry<EcosystemDefinition>> ECOSYSTEM =
            ResourceKey.createRegistryKey(new ResourceLocation(Wasteland.MOD_ID, "ecosystem"));

    @SubscribeEvent
    public static void registerDataRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(ModRegistries.ECOSYSTEM_TASK, EcosystemTaskRegistry.CODEC, EcosystemTaskRegistry.CODEC);
        event.dataPackRegistry(ModRegistries.ECOSYSTEM, EcosystemDefinition.CODEC, EcosystemDefinition.CODEC);
        Wasteland.LOGGER.info("Registering data pack registries");
    }
}
