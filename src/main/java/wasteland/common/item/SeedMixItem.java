package wasteland.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class SeedMixItem extends Item {
    private final Map<ResourceLocation, Integer> weights;
    private final int totalWeight;

    public SeedMixItem(Map<String, Double> weights) {
        super(new Item.Properties());
        if (weights.isEmpty()) throw new IllegalArgumentException("SeedMixItem needs at least one feature");
        this.weights = new HashMap<>();
        weights.forEach((key, value) -> this.weights.put(new ResourceLocation(key), value.intValue()));
        this.totalWeight = this.weights.values().stream().mapToInt(w -> Math.max(0, w)).sum();
        if (totalWeight <= 0) throw new IllegalArgumentException("Total weight must be > 0");
    }

    private ResourceLocation pick(RandomSource random) {
        int roll = random.nextInt(totalWeight);
        for (Map.Entry<ResourceLocation, Integer> e : weights.entrySet()) {
            roll -= Math.max(0, e.getValue());
            if (roll < 0) return e.getKey();
        }
        return weights.keySet().iterator().next(); // unreachable
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        ResourceLocation id = pick(level.getRandom());
        Optional<Holder.Reference<ConfiguredFeature<?, ?>>> holder = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolder(ResourceKey.create(Registries.CONFIGURED_FEATURE, id));

        if (holder.isEmpty()) return InteractionResult.FAIL;

        BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
        boolean placed = holder.get().value().place(
                level, level.getChunkSource().getGenerator(), level.getRandom(), pos);

        if (!placed) return InteractionResult.FAIL;

        Player player = ctx.getPlayer();
        if (player != null && !player.isCreative()) {
            ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
