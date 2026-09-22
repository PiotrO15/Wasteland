package wasteland.common.block;

import com.lowdragmc.mbd2.api.capability.MBDCapabilities;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Aquarium {
    private static final Map<Integer, List<BlockPos>> coralPositions = Map.of(
            0, List.of(new BlockPos(0, 1, 1), new BlockPos(1, 1, 3), new BlockPos(-1, 1, 2)),
            1, List.of(new BlockPos(2, 1, 2), new BlockPos(-1, 1, 3), new BlockPos(0, 1, 2)),
            2, List.of(new BlockPos(2, 1, 3), new BlockPos(3, 1, 2), new BlockPos(1, 1, 1))
    );

    public static void render(BlockEntity blockEntity, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        var optional = blockEntity.getCapability(MBDCapabilities.CAPABILITY_MACHINE).resolve();
        if (optional.isEmpty() || !(optional.get() instanceof MBDMachine machine)) return;
        if (!(machine.getTraitByName("aquarium_flora") instanceof ItemSlotCapabilityTrait trait)) return;

        Direction front = machine.getFrontFacing().orElse(Direction.NORTH);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(getRotationForFacing(front));
        stack.translate(-0.5, -0.5, -0.5);
        blockRenderer.renderSingleBlock(blockEntity.getBlockState(), stack, buffer, combinedLight, combinedOverlay);
        stack.popPose();

        for (int i = 0; i < trait.storage.getSlots(); i++) {
            ItemStack itemStack = trait.storage.getStackInSlot(i);
            if (itemStack.isEmpty()) continue;
            if (!(itemStack.getItem() instanceof BlockItem blockItem)) continue;

            BlockState fakeState = blockItem.getBlock().defaultBlockState();
            coralPositions.get(i).forEach(baseOffset -> {
                BlockPos rotatedOffset = rotateOffset(baseOffset, front);

                BlockPos worldPos = blockEntity.getBlockPos().offset(rotatedOffset);
                if (blockEntity.getLevel() == null) return;
                int light = LevelRenderer.getLightColor(blockEntity.getLevel(), worldPos);

                stack.pushPose();
                stack.translate(rotatedOffset.getX(), rotatedOffset.getY(), rotatedOffset.getZ());
                blockRenderer.renderSingleBlock(fakeState, stack, buffer, light, combinedOverlay, ModelData.EMPTY, RenderType.cutout());
                stack.popPose();
            });
        }
    }

    private static BlockPos rotateOffset(BlockPos offset, Direction front) {
        return switch (front) {
            case SOUTH -> offset.rotate(Rotation.CLOCKWISE_180);
            case EAST -> offset.rotate(Rotation.CLOCKWISE_90);
            case WEST -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
            default -> offset.rotate(Rotation.NONE); // NORTH
        };
    }

    public static Quaternionf getRotationForFacing(Direction front) {
        float degrees = switch (front) {
            case SOUTH -> 180f;
            case EAST -> -90f;
            case WEST -> 90f;
            default -> 0f; // NORTH
        };
        return new Quaternionf().rotateY((float) Math.toRadians(degrees));
    }
}
