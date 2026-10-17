package wasteland.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import wasteland.common.chunk.ChunkEventSystem;

public class BiodiversityScanner extends Item {
    public BiodiversityScanner(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            ChunkEventSystem.getInstance().notify(context.getClickedPos(), blockPos -> {
                Player player = context.getPlayer();
                if (player != null)
                    context.getPlayer().sendSystemMessage(Component.literal("Found connected ecostabilizer at: " + blockPos));
            });
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
