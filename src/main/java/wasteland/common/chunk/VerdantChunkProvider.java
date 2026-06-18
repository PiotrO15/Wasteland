package wasteland.common.chunk;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wasteland.Wasteland;

public class VerdantChunkProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    private final LevelChunk chunk;
    private final LazyOptional<VerdantChunk> lazyChunk = LazyOptional.of(this::getVerdantChunk);
    private VerdantChunk verdantChunk;

    public VerdantChunkProvider(LevelChunk chunk) {
        this.chunk = chunk;
    }

    private VerdantChunk getVerdantChunk() {
        if (this.verdantChunk == null) {
            this.verdantChunk = new VerdantChunk(this.chunk);
            Wasteland.LOGGER.log(Level.WARN, "Created VerdantChunk for chunk at {}", this.chunk.getPos());
        }
        return this.verdantChunk;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        return capability == Wasteland.VERDANT_CHUNK_CAPABILITY ? lazyChunk.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getVerdantChunk().serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getVerdantChunk().deserializeNBT(nbt);
    }
}
