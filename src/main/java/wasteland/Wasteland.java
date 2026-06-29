package wasteland;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wasteland.client.ClientSetup;
import wasteland.common.block.ModBlocks;
import wasteland.common.chunk.MBDEvents;
import wasteland.common.chunk.VerdantChunk;
import wasteland.common.chunk.VerdantChunkProvider;
import wasteland.common.entity.ModEntityTypes;
import wasteland.common.entity.drone.ProgWidgetPurify;
import wasteland.common.item.BiodiversityScanner;
import wasteland.common.item.Compost;
import wasteland.common.item.ModItems;
import wasteland.tree.BeehiveProbabilityModifier;

@Mod(Wasteland.MOD_ID)
public class Wasteland {
    public static final String MOD_ID = "wasteland";
    public static final Logger LOGGER = LogManager.getLogger("Wasteland");

    private static final DeferredRegister<ProgWidgetType<?>> PROG_WIDGETS_DEFERRED =
            DeferredRegister.create(PneumaticRegistry.RL("prog_widgets"), "wasteland");

    public Wasteland() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEntityTypes.register(modEventBus);

        modEventBus.addListener(ModBlocks::buildContents);
        modEventBus.addListener(ModItems::buildContents);

        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.onModConstruction();
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Compost.class);
        MinecraftForge.EVENT_BUS.register(BiodiversityScanner.class);
        MinecraftForge.EVENT_BUS.register(BeehiveProbabilityModifier.class);
        MinecraftForge.EVENT_BUS.register(MBDEvents.class);

        PROG_WIDGETS_DEFERRED.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ProgWidgetType<ProgWidgetPurify>> PURIFY =
            ModProgWidgets.PROG_WIDGETS_DEFERRED.register("purify", () -> ProgWidgetType.createType(ProgWidgetPurify::new));

    public static final Capability<VerdantChunk> VERDANT_CHUNK_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public void onAttachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(
                new ResourceLocation(MOD_ID, "verdant_chunk"),
                new VerdantChunkProvider(event.getObject())
        );
    }
}
