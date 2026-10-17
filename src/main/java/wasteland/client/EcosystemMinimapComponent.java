package wasteland.client;

import dev.ftb.mods.ftbchunks.api.client.minimap.MinimapContext;
import dev.ftb.mods.ftbchunks.api.client.minimap.MinimapInfoComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import wasteland.Wasteland;
import wasteland.common.block.ecostabilizer.Ecosystem;

public class EcosystemMinimapComponent implements MinimapInfoComponent {
    public static final ResourceLocation ID = new ResourceLocation(Wasteland.MOD_ID,"biome");

    private Ecosystem ecosystem;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void render(MinimapContext context, GuiGraphics graphics, Font font) {
        drawCenteredText(context.minecraft().font, graphics, Component.translatable("ecosystem." + ecosystem.getFriendlyName()), 0);
    }

    @Override
    public boolean shouldRender(MinimapContext context) {
        Level level = context.minecraft().level;
        Player player = context.minecraft().player;
        if (level == null || player == null) return false;
        Holder<Biome> biome = level.getBiome(player.blockPosition());
        if (biome.unwrapKey().isPresent()) {
            ecosystem = Ecosystem.fromAnchorBiome(biome);

            return ecosystem != null;
        }

        return false;
    }
}
