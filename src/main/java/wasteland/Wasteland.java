package wasteland;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import wasteland.block.ModBlocks;
import wasteland.item.Compost;
import wasteland.item.EssenceItem;
import wasteland.item.ModItems;
import wasteland.item.ResonantRod;

@Mod(Wasteland.MOD_ID)
public class Wasteland {
    public static final String MOD_ID = "wasteland";

    public Wasteland() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);

        modEventBus.addListener(ModBlocks::buildContents);
        modEventBus.addListener(ModItems::buildContents);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(EssenceItem.class);
        MinecraftForge.EVENT_BUS.register(Compost.class);
        MinecraftForge.EVENT_BUS.register(ResonantRod.class);
    }
}
