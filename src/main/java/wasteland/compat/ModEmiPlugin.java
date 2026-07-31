package wasteland.compat;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import wasteland.Wasteland;
import wasteland.common.registry.ModRegistries;

@EmiEntrypoint
public class ModEmiPlugin implements EmiPlugin {
    public static final EmiStack ECOSTABILIZER = EmiStack.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wasteland.MOD_ID, "ecostabilizer")));
    public static final EmiStack IMPROVED_ECOSTABILIZER = EmiStack.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wasteland.MOD_ID, "improved_ecostabilizer")));
    public static final EmiRecipeCategory ECOSYSTEM_TASK_CATEGORY
            = new EmiRecipeCategory(new ResourceLocation(Wasteland.MOD_ID, "ecosystem_task"), ECOSTABILIZER);

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ECOSYSTEM_TASK_CATEGORY);
        registry.addWorkstation(ECOSYSTEM_TASK_CATEGORY, ECOSTABILIZER);
        registry.addWorkstation(ECOSYSTEM_TASK_CATEGORY, IMPROVED_ECOSTABILIZER);

        RegistryAccess registryAccess = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : RegistryAccess.EMPTY;

        registryAccess.lookupOrThrow(ModRegistries.ECOSYSTEM_TASK).listElements().forEach(element -> {
            registry.addRecipe(new EcosystemTaskEmiRecipe(toRecipeId(element.key().location()), element.get(), registryAccess));
        });
    }

    public static ResourceLocation toRecipeId(ResourceLocation taskId) {
        return new ResourceLocation(taskId.getNamespace(), "/ecostabilizer_task/" + taskId.getPath());
    }
}
