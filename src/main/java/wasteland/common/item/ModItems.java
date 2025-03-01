package wasteland.common.item;

import me.desht.pneumaticcraft.common.item.DroneItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wasteland.Wasteland;
import wasteland.common.entity.drone.TerraformingDroneEntity;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Wasteland.MOD_ID);

    public static final RegistryObject<Item> COMPOST = ITEMS.register("compost", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ESSENCE_ITEM = ITEMS.register("essence_item", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> RESONANT_ROD = ITEMS.register("resonant_rod", () -> new Item(new Item.Properties().defaultDurability(192)));

    public static final RegistryObject<Item> TERRAFORMING_DRONE = ITEMS.register("terraforming_drone", () -> new DroneItem(TerraformingDroneEntity::new, false, DyeColor.LIME));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(COMPOST);
            event.accept(RESONANT_ROD);
        }
    }
}
