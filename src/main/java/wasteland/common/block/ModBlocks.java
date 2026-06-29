package wasteland.common.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wasteland.Wasteland;
import wasteland.common.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Wasteland.MOD_ID);

    public static final RegistryObject<Block> CRACKED_SAND = registerBlockItem("cracked_sand", new CrackedSand());
    public static final RegistryObject<Block> DEPLETED_SOIL = registerBlockItem("depleted_soil", new DepletedSoil(DepletedSoil.SoilState.DEPLETED, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> POOR_SOIL = registerBlockItem("poor_soil", new DepletedSoil(DepletedSoil.SoilState.POOR, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> RESTORING_SOIL = registerBlockItem("restoring_soil", new DepletedSoil(DepletedSoil.SoilState.RESTORING, BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
    public static final RegistryObject<Block> DEPLETED_SOIL_FARMLAND = registerBlockItem("depleted_soil_farmland", new DepletedSoilFarmland(BlockBehaviour.Properties.copy(Blocks.FARMLAND)));

    public static final BlockSetType DEAD_WOOD_BLOCK_SET_TYPE = BlockSetType.register(new BlockSetType("dead"));
    public static final WoodType DEAD_WOOD_TYPE = WoodType.register(new WoodType("dead", DEAD_WOOD_BLOCK_SET_TYPE));

    public static final RegistryObject<Block> DEAD_LOG = registerBlockItem("dead_log", new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)));
    public static final RegistryObject<Block> DEAD_PLANKS = registerBlockItem("dead_planks", new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)));
    public static final RegistryObject<Block> DEAD_STAIRS = registerBlockItem("dead_stairs", new StairBlock(Blocks.OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)));
    public static final RegistryObject<Block> DEAD_SLAB = registerBlockItem("dead_slab", new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB)));
    public static final RegistryObject<Block> DEAD_FENCE = registerBlockItem("dead_fence", new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE)));
    public static final RegistryObject<Block> DEAD_FENCE_GATE = registerBlockItem("dead_fence_gate", new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE), DEAD_WOOD_TYPE));
    public static final RegistryObject<Block> DEAD_DOOR = registerBlockItem("dead_door", new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR), DEAD_WOOD_BLOCK_SET_TYPE));
    public static final RegistryObject<Block> DEAD_TRAPDOOR = registerBlockItem("dead_trapdoor", new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR), DEAD_WOOD_BLOCK_SET_TYPE));
    public static final RegistryObject<Block> DEAD_PRESSURE_PLATE = registerBlockItem("dead_pressure_plate", new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE), DEAD_WOOD_BLOCK_SET_TYPE));
    public static final RegistryObject<Block> DEAD_BUTTON = registerBlockItem("dead_button", new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), DEAD_WOOD_BLOCK_SET_TYPE, 30, true));

    public static final RegistryObject<Block> FROSTED_DEAD_GRASS = registerBlockItem("frosted_dead_grass", new DeadGrass(6));
    public static final RegistryObject<Block> SHORT_DEAD_GRASS = registerBlockItem("short_dead_grass", new DeadGrass(4));
    public static final RegistryObject<Block> TALL_DEAD_GRASS = registerBlockItem("tall_dead_grass", new DeadGrass(8));
    public static final RegistryObject<Block> YELLOW_DEAD_GRASS = registerBlockItem("yellow_dead_grass", new DeadGrass(6));

    public static final RegistryObject<Block> CLOVER = registerBlockItem("clover", new CloverBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> WILDFLOWERS = registerBlockItem("wildflowers", new PinkPetalsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().sound(SoundType.PINK_PETALS).pushReaction(PushReaction.DESTROY)));
//    public static final RegistryObject<Block> LEAF_LITTER = registerBlockItem("leaf_litter", new PinkPetalsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollission().pushReaction(PushReaction.DESTROY)));

    public static final RegistryObject<Block> DEAD_MUSHROOM_STEM = registerBlockItem("dead_mushroom_stem", new Block(BlockBehaviour.Properties.copy(Blocks.MUSHROOM_STEM)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> registerBlockItem(String name, T block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block, new Item.Properties()));
        return registerBlock(name, () -> block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(CRACKED_SAND);
            event.accept(DEPLETED_SOIL);
            event.accept(POOR_SOIL);
            event.accept(RESTORING_SOIL);
            event.accept(DEPLETED_SOIL_FARMLAND);
            event.accept(DEAD_LOG);
            event.accept(FROSTED_DEAD_GRASS);
            event.accept(SHORT_DEAD_GRASS);
            event.accept(TALL_DEAD_GRASS);
            event.accept(YELLOW_DEAD_GRASS);

            event.accept(CLOVER);
            event.accept(WILDFLOWERS);
//            event.accept(LEAF_LITTER);
            event.accept(DEAD_MUSHROOM_STEM);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(DEAD_LOG);
            event.accept(DEAD_PLANKS);
            event.accept(DEAD_STAIRS);
            event.accept(DEAD_SLAB);
            event.accept(DEAD_FENCE);
            event.accept(DEAD_FENCE_GATE);
            event.accept(DEAD_DOOR);
            event.accept(DEAD_TRAPDOOR);
            event.accept(DEAD_PRESSURE_PLATE);
            event.accept(DEAD_BUTTON);
        }
    }
}
