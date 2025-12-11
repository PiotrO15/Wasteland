package wasteland;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.core.ModProgWidgets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import wasteland.client.ClientSetup;
import wasteland.common.block.ModBlocks;
import wasteland.common.entity.ModEntityTypes;
import wasteland.common.entity.drone.ProgWidgetPurify;
import wasteland.item.Compost;
import wasteland.item.ModItems;
import wasteland.tree.BeehiveProbabilityModifier;

@Mod(Wasteland.MOD_ID)
public class Wasteland {
    public static final String MOD_ID = "wasteland";

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
        MinecraftForge.EVENT_BUS.register(BeehiveProbabilityModifier.class);

        PROG_WIDGETS_DEFERRED.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ProgWidgetType<ProgWidgetPurify>> PURIFY =
            ModProgWidgets.PROG_WIDGETS_DEFERRED.register("purify", () -> ProgWidgetType.createType(ProgWidgetPurify::new));
}
