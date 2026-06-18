package wasteland.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.common.chunk.BlockGroup;
import wasteland.common.chunk.VerdantChunk;

public class BiodiversityScanner {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();

        if (itemStack.getItem() != ModItems.BIODIVERSITY_SCANNER.get()) {
            return;
        }

        event.getEntity().sendSystemMessage(
                Component.literal("Biodiversity: " +
                        VerdantChunk.getChunk(event.getPos(), level).getTotalCount(BlockGroup.GRASSES) + " " +
                        VerdantChunk.getChunk(event.getPos(), level).getLocalCount(event.getPos(), BlockGroup.GRASSES)));
    }
}
