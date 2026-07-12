package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;
import wasteland.common.block.ecostabilizer.EcosystemDefinition;
import wasteland.common.registry.ModRegistries;

import java.util.function.Consumer;

public class EcostabilizerEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(machineId))
            return;

        Ecosystem ecosystem = getOrCreateEcosystem(event.getMachine());

        if (ecosystem == null) {
            return;
        }

        int radius = getRadius(event.getMachine());

        ChunkEventSystem.getInstance().registerListener(event.getMachine(), radius);
        ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), radius, event.getMachine().getLevel());
        ChunkEventSystem.getInstance().computeBiomeStats(event.getMachine().getPos(), radius, event.getMachine().getLevel(), ecosystem.getAnchorTag());

        recalculateStages(event.getMachine().getPos(), event.getMachine(), event.getMachine().getLevel().registryAccess());

        Wasteland.LOGGER.warn("Computed data for machine formed with id {} at {}", event.getMachine().getDefinition().id(), event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onRemove(MachineRemovedEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().unregisterListener(event.getMachine().getPos());
        Wasteland.LOGGER.warn("Removed machine with id {}", event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onTick(MachineTickEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (event.getMachine().getLevel().getRandom().nextInt(100) != 0)
            return;

        if (!event.getMachine().getDefinition().id().equals(machineId))
            return;

        int biodiversity = ChunkEventSystem.getInstance().getBiodiversity(event.getMachine().getPos(), BlockGroup.GRASSES);

        if (biodiversity > 50) {
//            Wasteland.LOGGER.warn("Ticking machine with id {}, biodiversity good", event.getMachine().getDefinition().id());
        }
    }

    public static Ecosystem getEcosystem(MBDMachine machine) {
        String ecosystemName = machine.getCustomData().getString("ecosystem");

        if (ecosystemName.isEmpty()) {
            return null;
        } else {
            return Ecosystem.fromName(ecosystemName);
        }
    }

    public static Ecosystem getOrCreateEcosystem(MBDMachine machine) {
        String ecosystemName = machine.getCustomData().getString("ecosystem");

        if (ecosystemName.isEmpty()) {
            for (Ecosystem ecosystem : Ecosystem.values()) {
                if (ecosystem.matches(machine.getLevel().getBiome(machine.getPos()))) {
//                    machine.getCustomData().putString("ecosystem", ecosystem.getFriendlyName());
                    setCustomData(machine, compoundTag -> compoundTag.putString("ecosystem", ecosystem.getFriendlyName()));
                    return ecosystem;
                }
            }
        } else {
            return Ecosystem.fromName(ecosystemName);
        }

        return null;
    }

    public static int getRadius(MBDMachine machine) {
        int radius = machine.getCustomData().getInt("radius");
        Wasteland.LOGGER.warn("Getting radius for machine at {}, found: {}", machine.getPos(), radius);

        if (radius == 0) {
            radius = 24;
        }

        return radius;
    }

    public static int getStage(MBDMachine machine) {
        int stage = machine.getCustomData().getInt("transformation_stage");
        Wasteland.LOGGER.warn("Getting stage for machine at {}, found: {}", machine.getPos(), stage);

        if (stage == 0) {
            stage = 1;
        }

        return stage;
    }

    public static void setStage(MBDMachine machine, int stage) {
        int oldStage = getStage(machine);
        if (stage == oldStage)
            return;

        Ecosystem ecosystem = getEcosystem(machine);
        if (ecosystem == null)
            return;

        int oldRadius = getRadius(machine);

        int newRadius = oldRadius;
        newRadius = switch (stage) {
            case 1 -> 24;
            case 2 -> 40;
            case 3 -> 64;
            case 4 -> 80;
            case 5 -> 96;
            default -> newRadius;
        };

        if (oldRadius < newRadius) {
//            machine.getCustomData().putInt("radius", newRadius);
            int finalNewRadius = newRadius;
            setCustomData(machine, compoundTag -> compoundTag.putInt("radius", finalNewRadius));
        } else
            newRadius = oldRadius;

//        machine.getCustomData().putInt("transformation_stage", stage);
        setCustomData(machine, compoundTag -> compoundTag.putInt("transformation_stage", stage));
        ChunkEventSystem.getInstance().registerListener(machine, newRadius);
        ChunkEventSystem.getInstance().computeStats(machine.getPos(), newRadius, machine.getLevel());
        ChunkEventSystem.getInstance().computeBiomeStats(machine.getPos(), newRadius, machine.getLevel(), ecosystem.getAnchorTag());
    }

    public static void recalculateStages(BlockPos pos, MBDMachine machine, RegistryAccess registryAccess) {
        String ecosystemType = machine.getCustomData().getString("ecosystem");

        if (ecosystemType.isEmpty())
            return;

        Holder<EcosystemDefinition> ecosystem = registryAccess.lookupOrThrow(ModRegistries.ECOSYSTEM)
                .getOrThrow(ResourceKey.create(ModRegistries.ECOSYSTEM, new ResourceLocation(Wasteland.MOD_ID, ecosystemType)));

        int stageBefore = getStage(machine);

        Wasteland.LOGGER.warn("Starting stage recalculation at {}", pos);
        for (int i = 1; i < 5; i++) {
            boolean completed = true;
            for (var task : ecosystem.get().tasksForStage(i)) {
                if (!task.get().optional() && task.get().getProgress(pos) != 1) {
                    completed = false;
                    break;
                }
            }
            if (!completed || i == 4) {
                Wasteland.LOGGER.warn("Finished recalculating stage {} at {}, {}", i, pos, completed);
                setStage(machine, (completed && i == 4) ? 5 : i);
                break;
            }
        }

        if (getStage(machine) != stageBefore) {
            recalculateStages(pos, machine, registryAccess);
        }
    }

    public static void setCustomData(MBDMachine machine, Consumer<CompoundTag> mutator) {
        CompoundTag copy = machine.getCustomData().copy();
        mutator.accept(copy);
        machine.setCustomData(copy);
    }
}
