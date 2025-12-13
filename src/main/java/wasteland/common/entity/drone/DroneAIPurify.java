package wasteland.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockInteraction;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import wasteland.common.block.ModBlocks;

public class DroneAIPurify<W extends ProgWidgetAreaItemBase> extends DroneAIBlockInteraction<W> {
    public DroneAIPurify(IDroneBase drone, W progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isValidPosition(BlockPos blockPos) {
        Level level = drone.world();

        FluidStack fluidStack = drone.getFluidTank().getFluid();

        if (fluidStack.getAmount() < 250)
            return false;

        if (!fluidStack.getFluid().is(TagKey.create(BuiltInRegistries.FLUID.key(), new ResourceLocation("wasteland", "fertilizer"))))
            return false;

        return level.getBlockState(blockPos).getBlock() == ModBlocks.CRACKED_SAND.get() &&
                level.getBlockState(blockPos.above()).getBlock() != ModBlocks.CRACKED_SAND.get();
    }

    @Override
    protected boolean doBlockInteraction(BlockPos blockPos, double v) {
        Level level = drone.world();

        if (drone.getFluidTank().getFluidAmount() >= 250) {
            BlockPos lowestCrackedSand = blockPos;
            while (level.getBlockState(lowestCrackedSand.below()).getBlock() == ModBlocks.CRACKED_SAND.get()) {
                lowestCrackedSand = lowestCrackedSand.below();
            }

            level.setBlockAndUpdate(lowestCrackedSand, ModBlocks.DEPLETED_SOIL.get().defaultBlockState());

            drone.getFluidTank().getFluid().setAmount(drone.getFluidTank().getFluidAmount() - 250);
        }

        return false;
    }
}