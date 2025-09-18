package wasteland.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import wasteland.block.ModBlocks;

import java.util.Optional;

public class Compost {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();

        if (itemStack.getItem() != ModItems.COMPOST.get()) {
            return;
        }

        if (level.getBlockState(event.getPos()).is(TagKey.create(Registries.BLOCK, new ResourceLocation("wasteland", "depleted_soil")))) {
            BlockPos abovePos = event.getPos().above();
            if (level.getBlockState(abovePos).isAir()) {
                if (level.isClientSide()) {
                    BoneMealItem.addGrowthParticles(level, abovePos, 15);
                } else {
                    level.setBlockAndUpdate(abovePos, ModBlocks.CLOVER.get().defaultBlockState());

                    if (!event.getEntity().isCreative()) {
                        itemStack.shrink(1);
                    }
                }
            }
        }

        if (level.getBlockState(event.getPos()).getBlock() != Blocks.GRASS) {
            return;
        }

        if (level.isClientSide()) {
            BoneMealItem.addGrowthParticles(level, event.getPos(), 15);
        } else {
            Optional<Block> plant = ForgeRegistries.BLOCKS.tags().getTag(BlockTags.create(
                    new ResourceLocation("wasteland", "compost_growable"))).getRandomElement(level.random);

            if (plant.isPresent()) {
                BlockState plantState = plant.get().getStateForPlacement(new BlockPlaceContext(level, event.getEntity(), event.getHand(), itemStack, event.getHitVec()));

                if (plantState != null) {
                    level.setBlockAndUpdate(event.getPos(), plantState);

                    if (!event.getEntity().isCreative()) {
                        itemStack.shrink(1);
                    }
                }
            }
        }
    }
}
