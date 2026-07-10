package wasteland.common.chunk;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;
import wasteland.common.block.ModBlocks;

import java.awt.*;

public class MBDEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().registerListener(event.getMachine().getPos(), 3);
        ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), 12, event.getMachine().getLevel());
        Wasteland.LOGGER.warn("Computed data for machine formed with id {} at {}", event.getMachine().getDefinition().id(), event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onRemove(MachineRemovedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().unregisterListener(event.getMachine().getPos());
        Wasteland.LOGGER.warn("Removed machine with id {}", event.getMachine().getPos());
    }

    @SubscribeEvent
    public static void onTick(MachineTickEvent event) {
        if (event.getMachine().getLevel().getRandom().nextInt(100) != 0)
            return;

        if (!event.getMachine().getDefinition().id().equals(machineId))
            return;

        int biodiversity = ChunkEventSystem.getInstance().getBiodiversity(event.getMachine().getPos(), BlockGroup.GRASSES);

        if (biodiversity > 50) {
            Wasteland.LOGGER.warn("Ticking machine with id {}, biodiversity good", event.getMachine().getDefinition().id());
        }
    }

    @SubscribeEvent
    public static void onUI(MachineUIEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId))
            return;

        Widget tabWidget = event.getRoot().getFirstWidgetById("tabs");

        int transformationStage = event.getMachine().getCustomData().getInt("transformation_stage");

        if (tabWidget instanceof TabContainer tabs && transformationStage == 0) {
            TabButton stage1 = (TabButton) tabs.getFirstWidgetById("stage_1");
            TabButton stage2 = (TabButton) tabs.getFirstWidgetById("stage_2");
            TabButton stage3 = (TabButton) tabs.getFirstWidgetById("stage_3");
            TabButton stage4 = (TabButton) tabs.getFirstWidgetById("stage_4");

            WidgetGroup sg1 = tabs.tabs.get(stage1);
            if (sg1 != null) {
                DraggableScrollableWidgetGroup scrollableWidgetGroup = (DraggableScrollableWidgetGroup) sg1.getFirstWidgetById("task_group");

                scrollableWidgetGroup.addWidget(createTask(0, event.getMachine().getPos()));
                scrollableWidgetGroup.addWidget(createTask(28, event.getMachine().getPos()));
                scrollableWidgetGroup.addWidget(createTask(56, event.getMachine().getPos()));
                scrollableWidgetGroup.addWidget(createTask(84, event.getMachine().getPos()));
            }

            WidgetGroup sg2 = tabs.tabs.get(stage2);
            if (sg2 != null) {
                DraggableScrollableWidgetGroup scrollableWidgetGroup = (DraggableScrollableWidgetGroup) sg2.getFirstWidgetById("task_group");
                scrollableWidgetGroup.addWidget(createTask(0, event.getMachine().getPos()));
                scrollableWidgetGroup.addWidget(createTask(28, event.getMachine().getPos()));
                scrollableWidgetGroup.addWidget(createTask(56, event.getMachine().getPos()));
            }

            stage3.setOnPressCallback((clickData, aBoolean) -> stage3.setPressed(false));
            event.getRoot().addWidget(new ImageWidget(54, -28, 26, 32, () -> new ResourceTexture("wasteland:textures/gui/locked.png")));

            stage4.setOnPressCallback((clickData, aBoolean) -> stage4.setPressed(false));
            event.getRoot().addWidget(new ImageWidget(81, -28, 26, 32, () -> new ResourceTexture("wasteland:textures/gui/locked.png")));


            if (stage2 != null) {
                tabs.switchTag(tabs.tabs.get(stage2));
            }
        }
    }

    private static WidgetGroup createTask(int y, BlockPos pos) {
        WidgetGroup taskGroup = new WidgetGroup(0, y, 152, 24);
        taskGroup.setBackground(new ResourceTexture("wasteland:textures/gui/task_card.png"));

        ProgressWidget taskProgress = new ProgressWidget((() -> 0.5), 23, 15, 125, 4);
        taskProgress.setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        taskProgress.setProgressTexture(new ProgressTexture(new ResourceTexture("wasteland:textures/gui/small_bar_empty.png"), new ResourceTexture("wasteland:textures/gui/small_bar_filled.png")));
        taskGroup.addWidget(taskProgress);

        taskGroup.addWidget(new ImageWidget(3, 3, 16, 16, new ItemStackTexture(new ItemStack(ModBlocks.DEPLETED_SOIL.get()), new ItemStack(ModBlocks.CRACKED_SAND.get()))).appendHoverTooltips("Place blocks in Ecostabilizer's range"));

        TextTextureWidget progressText = new TextTextureWidget(23, 6, 125, 10,
                ChunkEventSystem.getInstance().getBiodiversity(pos, BlockGroup.GRASSES) + "/10000");
        progressText.getTextTexture()
                .setDropShadow(false)
                .setColor(0x333333);
        taskGroup.addWidget(progressText);

        ButtonWidget emiArea = new ButtonWidget(0, 0, 152, 24, IGuiTexture.EMPTY, clickData -> {
            EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(new ResourceLocation("jei:/mekanism/metallurgic_infusing/sand_to_dirt"));
            if (recipe != null) {
                EmiApi.displayRecipe(recipe);
            } else {
                Wasteland.LOGGER.warn("No recipe!");
            }
        });
        taskGroup.addWidget(emiArea);

        return taskGroup;
    }
}
