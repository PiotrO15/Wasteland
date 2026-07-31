package wasteland.common.block.ecostabilizer.task;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import wasteland.common.block.ecostabilizer.AnimalSpawner;
import wasteland.common.chunk.ChunkEventSystem;
import wasteland.common.registry.AnimalGroupRegistry;

import java.util.List;

public record AnimalEcosystemTask(int goal, TagKey<EntityType<?>> animalGroup, boolean optional, List<AnimalSpawner> animalSpawners) implements EcosystemTask {
    public static final ResourceLocation id = new ResourceLocation("wasteland", "animal_task");

    public AnimalEcosystemTask {
        AnimalGroupRegistry.register(animalGroup);
    }

    public static final MapCodec<AnimalEcosystemTask> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("goal").forGetter(AnimalEcosystemTask::goal),
                    TagKey.codec(Registries.ENTITY_TYPE).fieldOf("animal_group").forGetter(AnimalEcosystemTask::animalGroup),
                    Codec.BOOL.optionalFieldOf("optional", false).forGetter(AnimalEcosystemTask::optional),
                    AnimalSpawner.CODEC.listOf().optionalFieldOf("animal_spawners", List.of()).forGetter(AnimalEcosystemTask::animalSpawners)
            ).apply(instance, AnimalEcosystemTask::new)
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
        return ChunkEventSystem.getInstance().getAnimalCount(pos, animalGroup);
    }

    @Override
    public double getProgress(BlockPos pos) {
        return Math.min(1, (double) getProgressValue(pos) / getGoal());
    }

    @Override
    public boolean optional() {
        return optional;
    }

    @Override
    public List<AnimalSpawner> getAnimalSpawners() {
        return animalSpawners;
    }

    @Override
    public IGuiTexture getIcon(RegistryAccess registryAccess) {
        return IGuiTexture.MISSING_TEXTURE;
    }

    @Override
    public String[] getTooltip() {
        return new String[] {"Have at least " + getGoal() + " animals of given types"};
    }
}
