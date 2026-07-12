package wasteland.compat;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.block.ecostabilizer.task.BlockEcosystemTask;
import wasteland.common.block.ecostabilizer.task.EcosystemTask;

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
            widgets.addSlot(EmiIngredient.of(blockTask.getTag()), 86, 0);
            widgets.addText(Component.literal("Place " + task.getGoal() + " of these blocks"), 0, 20, 0x333333, false);
        }
    }
}
