package wasteland.common.block.ecostabilizer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wasteland.common.chunk.ChunkEventSystem;

import java.util.*;

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
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().location().equals(new ResourceLocation("minecraft", "overworld"))) return;
        if (event.getEntity() instanceof Mob mob) evaluate(mob);
    }

    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getLevel().dimension().location().equals(new ResourceLocation("minecraft", "overworld"))) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        Set<BlockPos> previous = affectedEcostabilizers.remove(mob.getUUID());
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
}
