package wasteland.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.progwidgets.*;
import me.desht.pneumaticcraft.common.entity.drone.AbstractBasicDroneEntity;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.DroneProgramBuilder;
import me.desht.pneumaticcraft.common.util.UpgradableItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wasteland.common.entity.ModEntityTypes;

import java.util.List;

public class TerraformingDroneEntity extends AbstractBasicDroneEntity {
    public TerraformingDroneEntity(EntityType<TerraformingDroneEntity> type, Level world) {
        super(type, world);
    }

    public TerraformingDroneEntity(Level world, Player player) {
        super(ModEntityTypes.TERRAFORMING_DRONE.get(), world, player);
    }

    void maybeAddStandbyInstruction(DroneProgramBuilder builder, ItemStack droneStack) {
        if (UpgradableItemUtils.getUpgradeCount(droneStack, ModUpgrades.STANDBY.get()) > 0) {
            builder.add(new ProgWidgetStandby());
        }
    }

    @Override
    public boolean addProgram(BlockPos clickPos, Direction facing, BlockPos pos, ItemStack droneStack, List<IProgWidget> widgets) {
        ProgWidgetPurify purifyPiece = new ProgWidgetPurify();
//        purifyPiece.setOrder(IBlockOrdered.Ordering.CLOSEST);

        DroneProgramBuilder builder = new DroneProgramBuilder();
        builder.add(new ProgWidgetStart());
        builder.add(new ProgWidgetJump(), ProgWidgetText.withText("loop"));
        widgets.addAll(builder.build());
//        maybeAddStandbyInstruction(builder, droneStack);
//        builder.add(new ProgWidgetWait(), ProgWidgetText.withText("10s"));

        DroneProgramBuilder loopBuilder = new DroneProgramBuilder();
        loopBuilder.add(new ProgWidgetLabel(), ProgWidgetText.withText("loop"));
        loopBuilder.add(new ProgWidgetLiquidImport(), ProgWidgetArea.fromPosition(clickPos, 2, 2,2));
        loopBuilder.add(purifyPiece, ProgWidgetArea.fromPosition(clickPos, 32, 16, 32));
        loopBuilder.add(new ProgWidgetJump(), ProgWidgetText.withText("loop"));
        widgets.addAll(loopBuilder.build());

        return true;
    }
}