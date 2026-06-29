package wasteland.common.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wasteland.Wasteland;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Wasteland.MOD_ID);

    public static final RegistryObject<BlockEntityType<?>> ECOSTABIILIZER = BLOCK_ENTITIES.register("ecostabilizer", () -> BlockEntityType.Builder.of(EcostabilizerBlockEntity::new, ModBlocks.ECOSTABILIZER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
