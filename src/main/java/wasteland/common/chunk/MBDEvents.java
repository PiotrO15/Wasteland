package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;

public class MBDEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().registerListener(event.getMachine().getPos(), 3);
        ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), 12, event.getMachine().getLevel());
        Wasteland.LOGGER.warn("Computed data for machine formed with id {} at {}", event.getMachine().getDefinition().id(), event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onRemove(MachineRemovedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().unregisterListener(event.getMachine().getPos());
        Wasteland.LOGGER.warn("Removed machine with id {}", event.getMachine().getPos());
    }
}
