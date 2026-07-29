package wasteland.compat;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import wasteland.common.block.ecostabilizer.task.AnimalEcosystemTask;
import wasteland.common.block.ecostabilizer.task.BiomeEcosystemTask;
import wasteland.common.block.ecostabilizer.task.BlockEcosystemTask;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;
import wasteland.common.item.ModItems;

public class EcosystemTaskEmiRecipe extends BasicEmiRecipe {
    private final EcosystemTask task;
    private final RegistryAccess registryAccess;

    public EcosystemTaskEmiRecipe(ResourceLocation id, EcosystemTask task, RegistryAccess registryAccess) {
        super(ModEmiPlugin.ECOSYSTEM_TASK_CATEGORY, id, 150, 54);
        this.task = task;
        this.registryAccess = registryAccess;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (task instanceof BlockEcosystemTask blockTask) {
            widgets.addSlot(EmiIngredient.of(blockTask.getTag()), 66, 9);
            widgets.addText(Component.literal("Place " + task.getGoal() + " of these blocks"), 75, 36, 0x333333, false).horizontalAlign(TextWidget.Alignment.CENTER);
        }

        if (task instanceof BiomeEcosystemTask biomeTask) {
            this.inputs.add(EmiIngredient.of(Ingredient.of(biomeTask.biomeType().equals(BiomeEcosystemTask.BiomeType.RECOVERING) ? ModItems.WEAK_ESSENCE.get() : ModItems.VERDANT_ESSENCE.get())));
            widgets.addSlot(getInputs().get(0), 66, 0);
            widgets.addText(Component.literal("Use essence in Ecostabilizer"), 75, 22, 0x333333, false).horizontalAlign(TextWidget.Alignment.CENTER);
            widgets.addText(Component.literal("to restore a total of at least"), 75, 32, 0x333333, false).horizontalAlign(TextWidget.Alignment.CENTER);
            widgets.addText(Component.literal(task.getGoal() + " blocks²"), 75, 42, 0x333333, false).horizontalAlign(TextWidget.Alignment.CENTER);
        }

        if (task instanceof AnimalEcosystemTask animalTask) {
            widgets.addTexture(new ResourceLocation("wasteland:textures/gui/entity_slot.png"), 58, 0, 34, 34, 0, 0);
            widgets.add(new EntityWidget(animalTask, 59, 1, 32, 32));
            widgets.addText(Component.literal("Have " + task.getGoal() + " of these animals"), 75, 40, 0x333333, false).horizontalAlign(TextWidget.Alignment.CENTER);
        }
    }
}
