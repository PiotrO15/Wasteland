package wasteland.common.chunk;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.machine.definition.config.event.*;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.EcosystemDefinition;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.registry.ModRegistries;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EcostabilizerEvents {
    private static final ResourceLocation machineId = new ResourceLocation("wasteland", "ecostabilizer");

    @SubscribeEvent
    public static void onFormed(MachineStructureFormedEvent event) {
        if (!event.getMachine().getDefinition().id().equals(machineId)) {
            return;
        }

        ChunkEventSystem.getInstance().registerListener(event.getMachine(), 3);
        ChunkEventSystem.getInstance().computeStats(event.getMachine().getPos(), 12, event.getMachine().getLevel());

        event.getMachine().getCustomData().putString("ecosystem", "temperate");

        if (!event.getMachine().getLevel().isClientSide())
            recalculateStages(event.getMachine().getPos(), event.getMachine(), event.getMachine().getLevel().registryAccess());

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
        MBDMachine machine = event.getMachine();
        if (!machine.getDefinition().id().equals(machineId))
            return;

        if (!event.getPlayer().level().isClientSide)
            event.getPlayer().level().registryAccess().lookupOrThrow(ModRegistries.ECOSYSTEM_TASK).listElementIds().forEach(id -> Wasteland.LOGGER.warn("Found Ecosystem task with id {}", id));

        Widget tabWidget = event.getRoot().getFirstWidgetById("tabs");

        int transformationStage = machine.getCustomData().getInt("transformation_stage");

        RegistryAccess registryAccess;
        if (event.getPlayer().level().isClientSide) {
            registryAccess = Minecraft.getInstance().level.registryAccess();
        } else {
            registryAccess = event.getPlayer().level().registryAccess();
        }

        if (tabWidget instanceof TabContainer tabs) {
            Holder<EcosystemDefinition> ecosystem = registryAccess
                    .lookupOrThrow(ModRegistries.ECOSYSTEM)
                    .getOrThrow(ResourceKey.create(ModRegistries.ECOSYSTEM, new ResourceLocation(Wasteland.MOD_ID, "temperate")));

            List<TabButton> stageWidgets = List.of(
                    (TabButton) tabs.getFirstWidgetById("stage_1"),
                    (TabButton) tabs.getFirstWidgetById("stage_2"),
                    (TabButton) tabs.getFirstWidgetById("stage_3"),
                    (TabButton) tabs.getFirstWidgetById("stage_4")
            );

            AtomicInteger computedStage = new AtomicInteger(1);
            stageWidgets.forEach(stage -> {
                int currentStage = computedStage.getAndIncrement();

                if (transformationStage < currentStage) {
                    stage.setOnPressCallback((clickData, aBoolean) -> stage.setPressed(false));
                    event.getRoot().addWidget(new ImageWidget(27 * currentStage - 27, -28, 26, 32, () -> new ResourceTexture("wasteland:textures/gui/locked.png")));
                    return;
                }

                WidgetGroup widgetGroup = tabs.tabs.get(stage);
                DraggableScrollableWidgetGroup scrollableWidgetGroup = (DraggableScrollableWidgetGroup) widgetGroup.getFirstWidgetById("task_group");

                AtomicInteger y = new AtomicInteger();
                ecosystem.get().tasksForStage(currentStage).forEach(task -> {
                    scrollableWidgetGroup.addWidget(createTaskCard(y.get(), event.getMachine().getPos(), task.value(), registryAccess, event.getPlayer().level().isClientSide));
                    y.addAndGet(28);
                });

                ProgressWidget stageProgress = (ProgressWidget) widgetGroup.getFirstWidgetById("stage_progress");
                stageProgress.setProgressSupplier(() -> {
                    var values = ecosystem.get().tasksForStage(currentStage).stream().toList();
                    double total = 0;
                    for (var value : values) {
                        total += value.get().getProgress(event.getMachine().getPos());
                    }
                    return total / values.size();
                });
            });

            tabs.switchTag(tabs.tabs.get(stageWidgets.get(transformationStage - 1)));
        }

        event.getRoot().addWidget(new ImageWidget(-20, 17, 16, 16, () -> new ResourceTexture("wasteland:textures/gui/temperate_ecosystem.png")).appendHoverTooltips("Found Ecosystem: Temperate", "", "Ecosystem is based on the nearby biomes.", "It cannot be changed."));
    }

    private static WidgetGroup createTaskCard(int y, BlockPos pos, EcosystemTask task, RegistryAccess registryAccess, boolean clientSide) {
        WidgetGroup taskGroup = new WidgetGroup(0, y, 152, 24);
        taskGroup.setBackground(new ResourceTexture("wasteland:textures/gui/task_card.png"));

        ProgressWidget taskProgress = new ProgressWidget(() -> task.getProgress(pos), 23, 15, 125, 4);
        taskProgress.setFillDirection(ProgressTexture.FillDirection.LEFT_TO_RIGHT);
        taskProgress.setProgressTexture(new ProgressTexture(new ResourceTexture("wasteland:textures/gui/small_bar_empty.png"), new ResourceTexture("wasteland:textures/gui/small_bar_filled.png")));
        taskGroup.addWidget(taskProgress);

        taskGroup.addWidget(new ImageWidget(3, 3, 16, 16, task.getIcon(registryAccess)).appendHoverTooltips(task.getTooltip()));

        TextTextureWidget progressText = new TextTextureWidget(23, 6, 125, 10,
                task.getProgressValue(pos) + "/" + task.getGoal());
        progressText.getTextTexture()
                .setDropShadow(false)
                .setColor(0x333333);
        taskGroup.addWidget(progressText);

        if (clientSide) {
            ButtonWidget emiArea = new ButtonWidget(0, 0, 152, 24, IGuiTexture.EMPTY, clickData -> {
                EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(task.getEntry());
                if (recipe != null) {
                    EmiApi.displayRecipe(recipe);
                } else {
                    Wasteland.LOGGER.warn("No recipe!");
                }
            });
            taskGroup.addWidget(emiArea);
        }

        return taskGroup;
    }

    public static void recalculateStages(BlockPos pos, MBDMachine machine, RegistryAccess registryAccess) {
        String ecosystemType = machine.getCustomData().getString("ecosystem");

        if (ecosystemType.isEmpty())
            return;

        Holder<EcosystemDefinition> ecosystem = registryAccess.lookupOrThrow(ModRegistries.ECOSYSTEM)
                .getOrThrow(ResourceKey.create(ModRegistries.ECOSYSTEM, new ResourceLocation(Wasteland.MOD_ID, ecosystemType)));

        for (int i = 1; i < 5; i++) {
            boolean completed = true;
            for (var task : ecosystem.get().tasksForStage(i)) {
                if (!task.get().optional() && task.get().getProgress(pos) != 1) {
                    completed = false;
                    break;
                }
            }
            if (!completed) {
                machine.getCustomData().putInt("transformation_stage", i);
                return;
            }
            if (i == 4) {
                machine.getCustomData().putInt("transformation_stage", i);
            }
        }
    }
}
