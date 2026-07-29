package wasteland.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.common.chunk.ChunkEventSystem;

public class BiodiversityScanner {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        ItemStack itemStack = event.getItemStack();

        if (itemStack.getItem() != ModItems.BIODIVERSITY_SCANNER.get()) {
            return;
        }

        ChunkEventSystem.getInstance().notify(event.getPos(), blockPos -> {
            event.getEntity().sendSystemMessage(Component.literal("Found connected ecostabilizer at: " + blockPos));
        });
    }
}
