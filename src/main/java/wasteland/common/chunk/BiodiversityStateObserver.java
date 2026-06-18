package wasteland.common.chunk;

import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Level;
import wasteland.Wasteland;

import java.util.Optional;

public class BiodiversityStateObserver {
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;

        Optional<BlockGroup> group = BlockGroup.fromState(event.getPlacedBlock());
        group.ifPresent(g -> {
            VerdantChunk verdantChunk = VerdantChunk.getChunk(event.getPos(), (net.minecraft.world.level.Level) event.getLevel());
            verdantChunk.increment(event.getPos(), g);
            Wasteland.LOGGER.info("Placed {} ({}): local={} total={}",
                    g, event.getPos(),
                    verdantChunk.getLocalCount(event.getPos(), g),
                    verdantChunk.getTotalCount(g));
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockGroup.fromState(event.getState()).ifPresent(g -> {
            VerdantChunk verdantChunk = VerdantChunk.getChunk(event.getPos(), (net.minecraft.world.level.Level) event.getLevel());
            verdantChunk.decrement(event.getPos(), g);
            Wasteland.LOGGER.log(Level.INFO, "Broken {} ({}): local={} total={}",
                    g, event.getPos(),
                    verdantChunk.getLocalCount(event.getPos(), g),
                    verdantChunk.getTotalCount(g));
        });
    }
}
