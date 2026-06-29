package wasteland.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import wasteland.Wasteland;
import wasteland.common.chunk.ChunkEventSystem;

public class EcostabilizerBlockEntity extends BlockEntity {
    private int whatever = 0;

    public EcostabilizerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(ModBlockEntities.ECOSTABIILIZER.get(), p_155229_, p_155230_);
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (getLevel() == null || getLevel().isClientSide())
            return;

        ChunkEventSystem.getInstance().registerListener(getBlockPos(), 3, this);
        whatever = ChunkEventSystem.getInstance().computeStats(getBlockPos(), 12, getLevel());
        Wasteland.LOGGER.warn("Loaded EcostabilizerBlockEntity with chunk biodiversity {}", whatever);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        if (getLevel() == null || getLevel().isClientSide())
            return;

        ChunkEventSystem.getInstance().unregisterListener(this);
    }

    public void increase(int amount) {
        if  (getLevel() == null || getLevel().isClientSide())
            return;

        whatever += amount;
        Wasteland.LOGGER.warn("Increased EcostabilizerBlockEntity with amount {}", amount);
    }

    public void decrease(int amount) {
        if  (getLevel() == null || getLevel().isClientSide())
            return;

        whatever -= amount;
        Wasteland.LOGGER.warn("Decreased EcostabilizerBlockEntity with amount {}", amount);
    }
}
