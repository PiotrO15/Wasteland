package wasteland.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wasteland.Wasteland;
import wasteland.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Wasteland.MOD_ID);

    public static final RegistryObject<Block> CRACKED_SAND = registerBlock("cracked_sand", () ->
            new Block(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> DEAD_LOG = registerBlock("dead_log", () ->
            new RotatedPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2.0F)));
    public static final RegistryObject<Block> FROSTED_DEAD_GRASS = registerBlock("frosted_dead_grass", () ->
            new DeadGrass(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.0F).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).replaceable(), 6));
    public static final RegistryObject<Block> SHORT_DEAD_GRASS = registerBlock("short_dead_grass", () ->
            new DeadGrass(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.0F).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).replaceable(), 4));
    public static final RegistryObject<Block> TALL_DEAD_GRASS = registerBlock("tall_dead_grass", () ->
            new DeadGrass(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.0F).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).replaceable(), 8));
    public static final RegistryObject<Block> YELLOW_DEAD_GRASS = registerBlock("yellow_dead_grass", () ->
            new DeadGrass(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.0F).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).replaceable(), 6));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> registryObject = BLOCKS.register(name, block);
        registerBlockItem(name, registryObject);
        return registryObject;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(CRACKED_SAND);
            event.accept(DEAD_LOG);
            event.accept(FROSTED_DEAD_GRASS);
            event.accept(SHORT_DEAD_GRASS);
            event.accept(TALL_DEAD_GRASS);
            event.accept(YELLOW_DEAD_GRASS);
        }
    }
}
