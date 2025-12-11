package wasteland.common.entity.drone;

import me.desht.pneumaticcraft.common.drone.IDroneBase;
import me.desht.pneumaticcraft.common.drone.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import wasteland.Wasteland;

public class ProgWidgetPurify extends ProgWidgetAreaItemBase {
    public ProgWidgetPurify() {
        super(Wasteland.PURIFY.get());
    }

    @Override
    public ResourceLocation getTexture() {
        return Textures.PROG_WIDGET_HARVEST;
    }

    public Goal getWidgetAI(IDroneBase drone, IProgWidget widget) {
        return new DroneAIPurify<>(drone, (ProgWidgetAreaItemBase) widget);
    }

    @Override
    public boolean hasStepInput() {
        return true;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIME;
    }

    @Override
    public WidgetDifficulty getDifficulty() {
        return WidgetDifficulty.ADVANCED;
    }
}