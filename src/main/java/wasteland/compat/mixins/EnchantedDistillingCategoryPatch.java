package wasteland.compat.mixins;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.favouriteless.enchanted.common.init.registry.EItems;
import net.favouriteless.enchanted.common.recipes.DistillingRecipe;
import net.favouriteless.enchanted.integrations.jei.categories.DistillingCategory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DistillingCategory.class)
public class EnchantedDistillingCategoryPatch {

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/favouriteless/enchanted/common/recipes/DistillingRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At("HEAD"), remap = false, cancellable = true)
    public void mixinSetRecipe(IRecipeLayoutBuilder builder, DistillingRecipe recipe, IFocusGroup focuses, CallbackInfo clr) {
        int offset = 20;

        boolean jar_handled = false;

        for(ItemStack i : recipe.getItemsIn()) {
            if (i.is(EItems.CLAY_JAR.get()) && !jar_handled) {
                builder.addSlot(RecipeIngredientRole.INPUT, 28, 30).addIngredient(VanillaTypes.ITEM_STACK, recipe.getItemsIn().get(0));
                jar_handled = true;
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, 50, offset).addIngredient(VanillaTypes.ITEM_STACK, i);

                offset += 20;
            }
        }

        offset = 0;

        for(ItemStack i : recipe.getItemsOut()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 2 + offset).addIngredient(VanillaTypes.ITEM_STACK, i);
            offset += 19;
        }

        clr.cancel();
    }
}
