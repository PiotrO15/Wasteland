package wasteland.common.entity;

import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wasteland.Wasteland;
import wasteland.common.entity.drone.TerraformingDroneEntity;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Wasteland.MOD_ID);

    public static final RegistryObject<EntityType<TerraformingDroneEntity>> TERRAFORMING_DRONE
            = register("terraforming_drone", ModEntityTypes::terraformingDrone);

    private static <E extends Entity> RegistryObject<EntityType<E>> register(final String name, final Supplier<EntityType.Builder<E>> sup) {
        return ENTITY_TYPES.register(name, () -> sup.get().build(name));
    }

    private static EntityType.Builder<TerraformingDroneEntity> terraformingDrone() {
        return EntityType.Builder.<TerraformingDroneEntity>of(TerraformingDroneEntity::new, MobCategory.CREATURE)
                .sized(0.7f, 0.35f)
                .setTrackingRange(32)
                .setUpdateInterval(3)
                .setCustomClientFactory(((spawnEntity, world) -> ModEntityTypes.TERRAFORMING_DRONE.get().create(world)))
                .setShouldReceiveVelocityUpdates(true);
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = Wasteland.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Listener {
        @SubscribeEvent
        public static void registerGlobalAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntityTypes.TERRAFORMING_DRONE.get(), DroneEntity.prepareAttributes().build());
        }
    }
}
