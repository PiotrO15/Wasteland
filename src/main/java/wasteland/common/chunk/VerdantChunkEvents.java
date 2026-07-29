package wasteland.common.chunk;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wasteland.Wasteland;

@Mod.EventBusSubscriber(modid = Wasteland.MOD_ID)
public class VerdantChunkEvents {
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getChunk() instanceof LevelChunk levelChunk)) return;

        levelChunk.getCapability(Wasteland.VERDANT_CHUNK_CAPABILITY).ifPresent(cap -> {
            if (cap.scannedHash() != VerdantChunk.CURRENT_SCAN_HASH_INDEX) {
                cap.scanChunk();
            }
        });
    }
}
