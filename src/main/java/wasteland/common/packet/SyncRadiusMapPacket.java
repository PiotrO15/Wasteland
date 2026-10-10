package wasteland.common.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import wasteland.Wasteland;
import wasteland.client.EcostabilizerRadiusMapCache;
import wasteland.common.chunk.ChunkEventSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncRadiusMapPacket {
    private final Map<BlockPos, Integer> machines;

    public SyncRadiusMapPacket(Map<BlockPos, Integer> machines) {
        this.machines = machines;
    }

    public static void encode(SyncRadiusMapPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.machines.size());
        packet.machines.forEach((pos, radius) -> {
            buf.writeBlockPos(pos);
            buf.writeVarInt(radius);
        });
    }

    public static SyncRadiusMapPacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        Map<BlockPos, Integer> map = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            map.put(buf.readBlockPos(), buf.readVarInt());
        }
        return new SyncRadiusMapPacket(map);
    }

    public static void handle(SyncRadiusMapPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EcostabilizerRadiusMapCache.set(packet.machines)));
        ctx.get().setPacketHandled(true);
    }

    public static void syncTo(ServerPlayer player) {
        Wasteland.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncRadiusMapPacket(new HashMap<>(ChunkEventSystem.getInstance().getMachineRadiusMap())));
    }

    public static void syncToAll() {
        Wasteland.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new SyncRadiusMapPacket(new HashMap<>(ChunkEventSystem.getInstance().getMachineRadiusMap())));
    }
}
