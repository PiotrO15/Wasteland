package wasteland.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import wasteland.common.block.DepletedSoil;

import java.util.Optional;

public class Compost {
    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickBlock event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();

        if (itemStack.getItem() != ModItems.COMPOST.get()) {
            return;
        }

        if (level.getBlockState(event.getPos()).getBlock() instanceof DepletedSoil) {
            if (!level.isClientSide()) {
                createClover((ServerLevel) level, level.getRandom(), event.getPos());
                if (!event.getEntity().isCreative()) {
                    itemStack.shrink(1);
                }
            } else {
                BoneMealItem.addGrowthParticles(level, event.getPos().above(), 15);
            }
            return;
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

    private static void createClover(ServerLevel level, RandomSource random, BlockPos pos) {
        Optional<Holder.Reference<PlacedFeature>> cloverFeature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation("wasteland", "clover")));
        Optional<Holder.Reference<PlacedFeature>> wildflowersFeature = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation("wasteland", "wildflowers")));

        BlockPos blockpos = pos.above();
        boolean placedWildflowers = false;

        label49:
        for(int i = 0; i < 32; ++i) {
            BlockPos blockpos1 = blockpos;

            for(int j = 0; j < i / 16; ++j) {
                blockpos1 = blockpos1.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                if (!(level.getBlockState(blockpos1.below()).getBlock() instanceof DepletedSoil) || level.getBlockState(blockpos1).isCollisionShapeFullBlock(level, blockpos1)) {
                    continue label49;
                }
            }

            BlockState blockstate1 = level.getBlockState(blockpos1);

            if (blockstate1.isAir()) {
                Holder<PlacedFeature> holder;

                if (random.nextInt(8) == 0 || !placedWildflowers) {
                    holder = wildflowersFeature.orElseThrow();
                    placedWildflowers = true;
                } else {
                    holder = cloverFeature.orElseThrow();
                }

                holder.value().place(level, level.getChunkSource().getGenerator(), random, blockpos1);
            }
        }
    }
}
