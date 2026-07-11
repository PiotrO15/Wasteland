package wasteland.common.chunk;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;
import wasteland.common.block.ecostabilizer.EcosystemDefinition;
import wasteland.common.registry.ModRegistries;

public class EcostabilizerEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        if (!event.getMachine().getLevel().isClientSide()) {

            Ecosystem ecosystem = getEcosystemType(event.getMachine());

            if (ecosystem == null) {
                return;
            }

            ChunkEventSystem.getInstance().registerListener(event.getMachine(), 3);
            ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), 12, event.getMachine().getLevel());
            ChunkEventSystem.getInstance().computeBiomeStats(event.getMachine().getPos(), 12, event.getMachine().getLevel(), ecosystem.getAnchorTag());

            recalculateStages(event.getMachine().getPos(), event.getMachine(), event.getMachine().getLevel().registryAccess());

            Wasteland.LOGGER.warn("Computed data for machine formed with id {} at {}", event.getMachine().getDefinition().id(), event.getMachine().getPos());
        }
    }

    @SubscribeEvent
    public static void onRemove(MachineRemovedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().unregisterListener(event.getMachine().getPos());
        Wasteland.LOGGER.warn("Removed machine with id {}", event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onTick(MachineTickEvent event) {
        if (event.getMachine().getLevel().getRandom().nextInt(100) != 0)
            return;

        if (!event.getMachine().getDefinition().id().equals(machineId))
            return;

        int biodiversity = ChunkEventSystem.getInstance().getBiodiversity(event.getMachine().getPos(), BlockGroup.GRASSES);

        if (biodiversity > 50) {
//            Wasteland.LOGGER.warn("Ticking machine with id {}, biodiversity good", event.getMachine().getDefinition().id());
        }
    }

    public static Ecosystem getEcosystemType(MBDMachine machine) {
        String ecosystemName = machine.getCustomData().getString("ecosystem");

        if (ecosystemName.isEmpty()) {
            for (Ecosystem ecosystem : Ecosystem.values()) {
                if (ecosystem.matches(machine.getLevel().getBiome(machine.getPos()))) {
                    machine.getCustomData().putString("ecosystem", ecosystem.getFriendlyName());
                    return ecosystem;
                }
            }
        } else {
            return Ecosystem.fromName(ecosystemName);
        }

        return null;
    }

    public static void recalculateStages(BlockPos pos, MBDMachine machine, RegistryAccess registryAccess) {
        String ecosystemType = machine.getCustomData().getString("ecosystem");

        if (ecosystemType.isEmpty())
            return;

        Holder<EcosystemDefinition> ecosystem = registryAccess.lookupOrThrow(ModRegistries.ECOSYSTEM)
                .getOrThrow(ResourceKey.create(ModRegistries.ECOSYSTEM, new ResourceLocation(Wasteland.MOD_ID, ecosystemType)));

        for (int i = 1; i < 5; i++) {
            boolean completed = true;
            for (var task : ecosystem.get().tasksForStage(i)) {
                if (!task.get().optional() && task.get().getProgress(pos) != 1) {
                    completed = false;
                    break;
                }
            }
            if (!completed) {
                machine.getCustomData().putInt("transformation_stage", i);
                return;
            }
            if (i == 4) {
                machine.getCustomData().putInt("transformation_stage", i);
            }
        }
    }
}
