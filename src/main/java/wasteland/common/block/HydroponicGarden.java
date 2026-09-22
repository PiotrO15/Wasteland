package wasteland.common.block;

import com.agricraft.agricraft.api.AgriApi;
import com.agricraft.agricraft.api.AgriClientApi;
import com.agricraft.agricraft.api.crop.AgriGrowthStage;
import com.agricraft.agricraft.api.genetic.AgriGenome;
import com.agricraft.agricraft.api.plant.AgriPlant;
import com.agricraft.agricraft.common.item.AgriSeedItem;
import com.lowdragmc.mbd2.api.capability.MBDCapabilities;
import com.lowdragmc.mbd2.api.recipe.MBDRecipeBuilder;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineAfterRecipeWorkingEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineBeforeRecipeWorkingEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineOnRecipeWorkingEvent;
import com.lowdragmc.mbd2.common.machine.definition.config.event.MachineRecipeModifyEvent;
import com.lowdragmc.mbd2.common.trait.item.ItemSlotCapabilityTrait;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.common.chunk.EcostabilizerEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static wasteland.common.block.Aquarium.getRotationForFacing;

@SuppressWarnings("unused")
public class HydroponicGarden {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "hydroponic_garden");

    @SubscribeEvent
    public static void onRecipe(MachineOnRecipeWorkingEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) return;

        EcostabilizerEvents.setCustomData(event.getMachine(), compoundTag -> compoundTag.putDouble("progress", event.getMachine().getRecipeLogic().getProgressPercent()));
    }

    @SubscribeEvent
    public static void afterRecipe(MachineAfterRecipeWorkingEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) return;

        EcostabilizerEvents.setCustomData(event.getMachine(), compoundTag -> compoundTag.putDouble("progress", 0.0));
    }

    public static void render(BlockEntity blockEntity, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        var optional = blockEntity.getCapability(MBDCapabilities.CAPABILITY_MACHINE).resolve();
        if (optional.isEmpty() || !(optional.get() instanceof MBDMachine machine)) return;
        if (!(machine.getTraitByName("item_input") instanceof ItemSlotCapabilityTrait trait)) return;

        Direction front = machine.getFrontFacing().orElse(Direction.NORTH);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        stack.pushPose();
        stack.translate(0.5, 0.5, 0.5);
        stack.mulPose(getRotationForFacing(front));
        stack.translate(-0.5, -0.5, -0.5);
        blockRenderer.renderSingleBlock(blockEntity.getBlockState(), stack, buffer, combinedLight, combinedOverlay);
        stack.popPose();

        ItemStack itemStack = trait.storage.getStackInSlot(0);
        if (itemStack.getItem() instanceof AgriSeedItem) {
            int stage = (int) (machine.getCustomData().getDouble("progress") * 8.0);
            BakedModel model = AgriClientApi.getPlantModel(AgriSeedItem.getSpecies(itemStack), stage);

            if (model != null) {
                stack.translate(0, 1, 0);
                int color = Minecraft.getInstance().getBlockColors().getColor(blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(), 0);
                if (color == -1) {
                    blockRenderer.getModelRenderer().renderModel(stack.last(), buffer.getBuffer(RenderType.cutoutMipped()), blockEntity.getBlockState(), model, 1.0F, 1.0F, 1.0F, combinedLight, combinedOverlay);
                } else {
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8 & 255) / 255.0F;
                    float b = (float)(color & 255) / 255.0F;
                    blockRenderer.getModelRenderer().renderModel(stack.last(), buffer.getBuffer(RenderType.cutoutMipped()), blockEntity.getBlockState(), model, r, g, b, combinedLight, combinedOverlay);
                }
            }
        }
    }

    @SubscribeEvent
    public static void beforeRecipe(MachineBeforeRecipeWorkingEvent event) {
        if (event.getMachine().getLevel().isClientSide()) return;
        if (!event.getMachine().getDefinition().id().equals(machineId)) return;
        if (!(event.getMachine().getTraitByName("item_input") instanceof ItemSlotCapabilityTrait trait)) return;

        ItemStack itemStack = trait.storage.getStackInSlot(0);

        if (event.getRecipe().getId().toString().equals("wasteland:seeds")) {
            if (itemStack.getItem() instanceof AgriSeedItem) {
                AgriGenome.fromNBT(itemStack.getTag());
                AgriGenome genome = AgriGenome.fromNBT(itemStack.getTag());
                Optional<AgriPlant> plant = AgriApi.getPlant(AgriSeedItem.getSpecies(itemStack));

                if (genome != null && plant.isPresent()) return;
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void beforeModifyRecipe(MachineRecipeModifyEvent.Before event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) return;
        if (!event.getRecipe().getId().toString().equals("wasteland:seeds")) return;
        if (!(event.getMachine().getTraitByName("item_input") instanceof ItemSlotCapabilityTrait trait)) return;

        ItemStack itemStack = trait.storage.getStackInSlot(0);

        if (itemStack.getItem() instanceof AgriSeedItem) {
            List<ItemStack> drops = new ArrayList<>();

            AgriGenome genome = AgriGenome.fromNBT(itemStack.getTag());
            if (genome == null) return;

            AgriPlant plant = AgriApi.getPlant(AgriSeedItem.getSpecies(itemStack)).orElseThrow();

            for (int trials = (genome.getGain() + 3) / 3; trials > 0; --trials) {
                plant.getHarvestProducts(drops::add, new AgriGrowthStage(7, 8), genome, event.getMachine().getLevel().random);
            }

            MBDRecipeBuilder builder = event.getRecipe().toBuilder();

            builder.output.clear();
            drops.forEach(builder::outputItems);

            event.setRecipe(builder.buildRawRecipe());
        }
    }
}
