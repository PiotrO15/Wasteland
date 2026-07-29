package wasteland.compat;

import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.lowdragmc.mbd2.api.recipe.ingredient.EntityIngredient;
import com.lowdragmc.mbd2.common.gui.recipe.ingredient.entity.EntityPreviewWidget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import wasteland.common.block.ecostabilizer.task.AnimalEcosystemTask;

import java.util.ArrayList;
import java.util.List;

public class EntityWidget extends Widget {
    private final AnimalEcosystemTask animalTask;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<Entity> entities = new ArrayList<>();

    public EntityWidget(AnimalEcosystemTask animalEcosystemTask, int x, int y, int width, int height) {
        this.animalTask = animalEcosystemTask;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        TrackedDummyWorld dummyWorld = new TrackedDummyWorld();

        EntityIngredient entityIngredient = EntityIngredient.of(animalTask.animalGroup(), animalTask.getGoal());
        for (var entityType : entityIngredient.getTypes()) {
            var entity = entityType.create(dummyWorld);
            if (entity != null) {
                if (entityIngredient.getNbt() != null) {
                    var tag = entity.serializeNBT();
                    tag.merge(entityIngredient.getNbt());
                    entity.load(tag);
                }
                entities.add(entity);
            }
        }
    }

    @Override
    public Bounds getBounds() {
        return new Bounds(this.x, this.y, this.width, this.height);
    }

    @Nullable
    public Entity getCurrentEntity() {
        if (entities.isEmpty()) return null;
        var index = Math.abs((int)(System.currentTimeMillis() / 1000) % entities.size());
        return entities.get(index);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        var entity = getCurrentEntity();
        if (entity == null) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 50);

        double scaleH = height / entity.getBbHeight();
        double scaleW = width / entity.getBbWidth();
        EntityPreviewWidget.renderEntityInInventory(
                guiGraphics, x + width / 2, y + height / 2,
                Math.min(scaleW, scaleH) * 0.45, entity);

        guiGraphics.pose().popPose();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (!getBounds().contains(mouseX, mouseY)) return List.of();
        var entity = getCurrentEntity();
        if (entity == null) return List.of();
        return List.of(ClientTooltipComponent.create(entity.getDisplayName().getVisualOrderText()));
    }
}
