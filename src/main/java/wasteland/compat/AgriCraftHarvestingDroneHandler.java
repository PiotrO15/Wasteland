package wasteland.compat;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.crop.AgriCrop;
import com.agricraft.agricraft.common.registry.ModItems;
import me.desht.pneumaticcraft.api.drone.IDrone;
import me.desht.pneumaticcraft.api.harvesting.HarvestHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgriCraftHarvestingDroneHandler extends HarvestHandler {
    AgriCraftHarvestingDroneHandler() {
    }

    @Override
    public boolean canHarvest(Level level, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        return AgriApi.getCrop(level, pos).map(AgriCrop::canBeHarvested).orElse(false);
    }

    @Override
    public void harvest(Level level, BlockGetter chunkCache, BlockPos pos, BlockState state, IDrone drone) {
        ItemStack heldItem = drone.getInv().getStackInSlot(0);

        if (heldItem.is(ModItems.CLIPPER.get())) {
            List<ItemStack> drops = new ArrayList<>();

            Optional<AgriCrop> optionalCrop = AgriApi.getCrop(level, pos);
            if (optionalCrop.isPresent()) {
                AgriCrop crop = optionalCrop.get();
                crop.getClippingProducts(drops::add, heldItem);
                crop.setGrowthStage(crop.getPlant().getInitialGrowthStage());
                for (ItemStack drop : drops) {
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                }
                crop.getPlant().onClipped(crop, heldItem, (LivingEntity) drone);
            }
        } else {
            AgriApi.getCrop(level, pos).ifPresent(crop -> crop.harvest(drop -> this.spawnEntity(level, pos, drop), null));
        }
    }

    private void spawnEntity(Level world, BlockPos pos, ItemStack stack) {
        double x = pos.getX() + 0.5 + 0.25 * world.getRandom().nextDouble();
        double y = pos.getY() + 0.5 + 0.25 * world.getRandom().nextDouble();
        double z = pos.getZ() + 0.5 + 0.25 * world.getRandom().nextDouble();
        world.addFreshEntity(new ItemEntity(world, x, y, z, stack));
    }
}
