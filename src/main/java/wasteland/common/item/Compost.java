package wasteland.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import wasteland.common.block.DepletedSoil;
import wasteland.common.block.ecostabilizer.Ecosystem;

import java.util.Optional;

public class Compost extends BoneMealItem {
    public Compost(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean creative = context.getPlayer() != null && context.getPlayer().isCreative();

        if (level.getBlockState(pos).getBlock() instanceof DepletedSoil) {
            if (level.getBlockState(pos.above()).isAir()) {
                if (level.isClientSide()) {
                    BoneMealItem.addGrowthParticles(level, pos.above(), 15);
                } else {
                    createClover((ServerLevel) level, level.getRandom(), pos);
                    if (!creative) {
                        itemStack.shrink(1);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        } else if (level.getBlockState(pos).getBlock() == Blocks.GRASS) {
            if (level.isClientSide()) {
                BoneMealItem.addGrowthParticles(level, pos, 15);
            } else {
                Ecosystem ecosystem = Ecosystem.fromAnchorBiome(level.getBiome(pos));
                Block sapling;

                if (ecosystem != null) {
                    sapling = ForgeRegistries.BLOCKS.tags().getTag(ecosystem.getSaplingTag()).getRandomElement(level.random).orElse(Blocks.OAK_SAPLING);
                } else {
                    sapling = Blocks.OAK_SAPLING;
                }

                BlockState plantState = sapling.defaultBlockState();

                level.setBlockAndUpdate(pos, plantState);

                if (!creative) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useOn(context);
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
