package wasteland.common.chunk;

import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineOnLoadEvent;
import com.lowdragmc.mbd2.integration.kubejs.events.MBDMachineEvents;
import com.lowdragmc.mbd2.integration.kubejs.events.MBDServerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MBDEvents {
    private static ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public void onLoad(MachineOnLoadEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }


    }
}
