package wasteland.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.ai.DroneAIBlockInteraction;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import wasteland.common.block.ModBlocks;

public class DroneAIPurify<W extends ProgWidgetAreaItemBase> extends DroneAIBlockInteraction<W> {
    public DroneAIPurify(IDroneBase drone, W progWidget) {
        super(drone, progWidget);
    }

    @Override
    protected boolean isValidPosition(BlockPos blockPos) {
        Level level = drone.world();

        boolean foundItem = false;
        for (int slot = 0; slot < drone.getInv().getSlots(); slot++) {
            ItemStack droneStack = drone.getInv().getStackInSlot(slot);
            if (droneStack.getItem() == Items.BONE_MEAL) {
                foundItem = true;
                break;
            }
        }
        if (!foundItem) return false;

        FluidStack fluidStack = drone.getFluidTank().getFluid();
        if (!fluidStack.getFluid().isSame(Fluids.WATER) || fluidStack.getAmount() < 250)
            return false;

        return level.getBlockState(blockPos).getBlock() == ModBlocks.CRACKED_SAND.get() &&
                level.getBlockState(blockPos.above()).getBlock() != ModBlocks.CRACKED_SAND.get();
    }

    @Override
    protected boolean doBlockInteraction(BlockPos blockPos, double v) {
        Level level = drone.world();
        for (int slot = 0; slot < drone.getInv().getSlots(); slot++) {
            ItemStack droneStack = drone.getInv().getStackInSlot(slot);

            if (droneStack.getItem() == Items.BONE_MEAL && drone.getFluidTank().getFluidAmount() >= 250) {
                BlockPos lowestCrackedSand = blockPos;
                while (level.getBlockState(lowestCrackedSand.below()).getBlock() == ModBlocks.CRACKED_SAND.get()) {
                    lowestCrackedSand = lowestCrackedSand.below();
                }

                level.setBlockAndUpdate(lowestCrackedSand, Blocks.DIRT.defaultBlockState());
                drone.getInv().getStackInSlot(slot).setCount(droneStack.getCount() - 1);

                drone.getFluidTank().getFluid().setAmount(drone.getFluidTank().getFluidAmount() - 250);
            }
        }

        return false;
    }
}
