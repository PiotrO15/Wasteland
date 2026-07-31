package wasteland.common.block.ecostabilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wasteland.common.chunk.ChunkEventSystem;

import java.util.*;

@Mod.EventBusSubscriber
public class AnimalEvents {
    private final Map<UUID, Set<BlockPos>> affectedEcostabilizers = new HashMap<>();

    private static AnimalEvents instance;
    public static AnimalEvents getInstance() {
        if (instance == null) instance = new AnimalEvents();
        return instance;
    }

    public void onPositionChanged(Mob mob) {
        evaluate(mob);
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().location().equals(new ResourceLocation("minecraft", "overworld"))) return;
        if (event.loadedFromDisk()) return;
        if (event.getEntity() instanceof Mob mob) AnimalEvents.getInstance().evaluate(mob);
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().location().equals(new ResourceLocation("minecraft", "overworld"))) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        Entity.RemovalReason reason = mob.getRemovalReason();
        if (reason == null) return;
        boolean isRealRemoval = reason == Entity.RemovalReason.KILLED
                || reason == Entity.RemovalReason.DISCARDED
                || reason == Entity.RemovalReason.CHANGED_DIMENSION;
        if (!isRealRemoval) return;

        Set<BlockPos> previous = AnimalEvents.getInstance().affectedEcostabilizers.remove(mob.getUUID());
        if (previous == null) return;
        for (BlockPos stabilizer : previous) {
            ChunkEventSystem.getInstance().notifyMobExit(stabilizer, mob);
        }
    }

    private void evaluate(Mob mob) {
        UUID id = mob.getUUID();
        Set<BlockPos> previous = affectedEcostabilizers.getOrDefault(id, Set.of());
        Set<BlockPos> current = new HashSet<>();

        for (var entry : ChunkEventSystem.getInstance().getMachineRadiusMap().entrySet()) {
            if (ChunkEventSystem.withinRadius(entry.getKey(), mob.blockPosition(), entry.getValue())) {
                current.add(entry.getKey());
            }
        }

        for (BlockPos stabilizer : current) {
            if (!previous.contains(stabilizer)) ChunkEventSystem.getInstance().notifyMobEnter(stabilizer, mob);
        }
        for (BlockPos stabilizer : previous) {
            if (!current.contains(stabilizer)) ChunkEventSystem.getInstance().notifyMobExit(stabilizer, mob);
        }

        affectedEcostabilizers.put(id, current);
    }

    public void registerInitial(BlockPos stabilizer, Mob mob) {
        affectedEcostabilizers
                .computeIfAbsent(mob.getUUID(), k -> new HashSet<>())
                .add(stabilizer);
    }
}
