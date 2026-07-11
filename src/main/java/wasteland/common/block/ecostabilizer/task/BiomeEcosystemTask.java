package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import wasteland.common.chunk.ChunkEventSystem;

import java.util.Set;

public record BiomeEcosystemTask(int goal, BiomeType biomeType, ResourceLocation entry, boolean optional) implements EcosystemTask {
    public static final ResourceLocation id = new ResourceLocation("wasteland", "biome_task");

    public static final MapCodec<BiomeEcosystemTask> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("goal").forGetter(BiomeEcosystemTask::goal),
                    BiomeType.CODEC.fieldOf("biome_type").forGetter(BiomeEcosystemTask::biomeType),
                    ResourceLocation.CODEC.fieldOf("entry").forGetter(BiomeEcosystemTask::entry),
                    Codec.BOOL.optionalFieldOf("optional", false).forGetter(BiomeEcosystemTask::optional)
            ).apply(instance, BiomeEcosystemTask::new)
    );

    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public int getGoal() {
        return goal;
    }

    @Override
    public int getProgressValue(BlockPos pos) {
        return ChunkEventSystem.getInstance().getBiomeCount(pos, biomeType);
    }

    @Override
    public double getProgress(BlockPos pos) {
        return Math.min(1, (double) getProgressValue(pos) / getGoal());
    }

    @Override
    public ResourceLocation getEntry() {
        return entry;
    }

    @Override
    public boolean optional() {
        return optional;
    }

    @Override
    public IGuiTexture getIcon(RegistryAccess registryAccess) {
        ResourceLocation id = new ResourceLocation("wasteland", biomeType == BiomeType.RECOVERING ? "textures/gui/recovering_biome_task.png" : "textures/gui/verdant_biome_task.png");
        return new ResourceTexture(id);
    }

    @Override
    public String[] getTooltip() {
        return new String[] {"Restore an area of at least " + getGoal() + " blocks² to " + biomeType.name().toLowerCase() + " stage"};
    }

    public enum BiomeType {
        RECOVERING(Set.of("recovering", "minecraft")),
        VERDANT(Set.of("minecraft"));

        private final Set<String> allowedNamespaces;

        BiomeType(Set<String> allowedNamespaces) {
            this.allowedNamespaces = allowedNamespaces;
        }

        public Set<String> allowedNamespaces() {
            return allowedNamespaces;
        }

        public static final Codec<BiomeType> CODEC =
                Codec.STRING.xmap(
                        BiomeType::valueOf,
                        BiomeType::toString
                );
    }
}
