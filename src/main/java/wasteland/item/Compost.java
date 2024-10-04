package wasteland.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class Compost {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();

        if (itemStack.getItem() != ModItems.COMPOST.get()) {
            return;
        }

        // Transform grass into a random sapling
        if (level.getBlockState(event.getPos()).getBlock() == Blocks.GRASS) {
            if (level.isClientSide()) {
                // Spawn particles
                BoneMealItem.addGrowthParticles(level, event.getPos(), 15);
            } else {
                Optional<Block> plant = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(
                        new ResourceLocation("wasteland", "compost_growable"))).getRandomElement(level.random);

                if (plant.isPresent()) {
                    level.setBlockAndUpdate(event.getPos(), plant.get().defaultBlockState());

                    if (!event.getEntity().isCreative()) {
                        itemStack.shrink(1);
                    }
                }
            }
        }
    }
}
