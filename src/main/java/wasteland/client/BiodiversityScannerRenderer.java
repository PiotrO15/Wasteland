package wasteland.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import wasteland.Wasteland;
import wasteland.common.chunk.ChunkEventSystem;
import wasteland.common.item.ModItems;

import java.util.*;

import static wasteland.common.chunk.ChunkEventSystem.asQuart;
import static wasteland.common.chunk.ChunkEventSystem.quantize;

@Mod.EventBusSubscriber(modid = Wasteland.MOD_ID, value = Dist.CLIENT)
public class BiodiversityScannerRenderer {
    // TODO: Refactor and simplify code

    private static final RenderType AREA_TYPE = createRenderType();

    private static int lastCoverageHash;
    private static List<WallEntry> cachedWalls;

    public enum ZoneType { ECOSTABILIZER, EXTENDER }

    private static final int[][] DIRS = {{0, -4}, {0, 4}, {4, 0}, {-4, 0}};

    public static RenderType createRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("<=", 515))
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLayeringState(new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();
                    posestack.scale(0.99975586F, 0.99975586F, 0.99975586F);
                    RenderSystem.applyModelViewMatrix();
                }, () -> {
                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }))
                .createCompositeState(true);
        return RenderType.create("ecostabilizer_area", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    private static List<BlockPos> affectedQuartCells(Level level, BlockPos center, int radius) {
        List<BlockPos> cells = new ArrayList<>();
        int quartRadius = asQuart(radius); // reuse your existing helper
        BlockPos origin = quantize(center);
        int originQx = origin.getX() / 4, originQz = origin.getZ() / 4;

        for (int dx = -quartRadius; dx <= quartRadius; dx++) {
            for (int dz = -quartRadius; dz <= quartRadius; dz++) {
                if (dx * dx + dz * dz <= (quartRadius + 0.5) * (quartRadius + 0.5)) {
                    int x = (originQx + dx) * 4;
                    int z = (originQz + dz) * 4;
                    if (!level.isLoaded(new BlockPos(x, 0, z))) continue;
                    cells.add(new BlockPos((originQx + dx) * 4, center.getY() - 1, (originQz + dz) * 4));
                }
            }
        }
        return cells;
    }

    private static Map<BlockPos, Set<ZoneType>> buildCoverageMap(Level level) {
        Map<BlockPos, Set<ZoneType>> coverage = new HashMap<>();

        for (var entry : ChunkEventSystem.getInstance().getMachineRadiusMap().entrySet()) {
            for (BlockPos cell : affectedQuartCells(level, entry.getKey(), entry.getValue())) {
                coverage.computeIfAbsent(cell, k -> EnumSet.noneOf(ZoneType.class)).add(ZoneType.ECOSTABILIZER);
            }
        }
//        for (var entry : ClientRadiusCache.extenderZones().entrySet()) {
//            for (BlockPos cell : affectedQuartCells(entry.getKey(), entry.getValue())) {
//                coverage.computeIfAbsent(cell, k -> EnumSet.noneOf(ZoneType.class)).add(ZoneType.EXTENDER);
//            }
//        }
        return coverage;
    }

    private static int colorFor(Set<ZoneType> types) {
        if (types.size() > 1) return 0x80FFFF00; // overlap = distinct color (e.g. yellow), not a blend
        return switch (types.iterator().next()) {
            case ECOSTABILIZER -> 0x4000AAFF; // translucent blue
            case EXTENDER      -> 0x40FF6020; // translucent orange
        };
    }

    private static void rebuildIfNeeded() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Map<BlockPos, Set<ZoneType>> coverage = buildCoverageMap(level);
        int hash = coverage.hashCode();
        if (hash == lastCoverageHash) return;

        cachedWalls = buildWalls(coverage, level);
        lastCoverageHash = hash;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || (!player.getMainHandItem().is(ModItems.BIODIVERSITY_SCANNER.get()) && !player.getOffhandItem().is(ModItems.BIODIVERSITY_SCANNER.get()))) return;

        rebuildIfNeeded();

        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);


        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(AREA_TYPE);
        Matrix4f mat = poseStack.last().pose();

        for (WallEntry w : cachedWalls) {
            int c = w.color();
            float a = ((c >> 24) & 0xFF) / 255f, r = ((c >> 16) & 0xFF) / 255f,
                    g = ((c >> 8) & 0xFF) / 255f,  b = (c & 0xFF) / 255f;
            float wallAlpha = Math.min(1.0f, a * 2.2f);

            float x1 = w.edgeStart().getX(), z1 = w.edgeStart().getZ();
            float x2 = w.edgeEnd().getX(),   z2 = w.edgeEnd().getZ();
            float yBottom = -64;
            float yTop = 320;

            buffer.vertex(mat, x1, yBottom, z1).color(r, g, b, wallAlpha).endVertex();
            buffer.vertex(mat, x1, yTop,    z1).color(r, g, b, wallAlpha).endVertex();
            buffer.vertex(mat, x2, yTop,    z2).color(r, g, b, wallAlpha).endVertex();
            buffer.vertex(mat, x2, yBottom, z2).color(r, g, b, wallAlpha).endVertex();
        }

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(AREA_TYPE);
        poseStack.popPose();
    }

    record WallEntry(BlockPos edgeStart, BlockPos edgeEnd, int color) {}

    private static List<WallEntry> buildWalls(Map<BlockPos, Set<ZoneType>> coverage, Level level) {
        List<WallEntry> walls = new ArrayList<>();

        for (var entry : coverage.entrySet()) {
            BlockPos cell = entry.getKey();
            Set<ZoneType> types = entry.getValue();

            for (int[] dir : DIRS) {
                BlockPos neighbor = new BlockPos(cell.getX() + dir[0], cell.getY(), cell.getZ() + dir[1]);
                Set<ZoneType> neighborTypes = coverage.get(neighbor);

                boolean isBoundary = neighborTypes == null || !neighborTypes.equals(types);
                if (!isBoundary) continue;

                int y = 0;
                int color = colorFor(types);

                BlockPos p1, p2;
                if (dir[0] == 0) {
                    int edgeZ = cell.getZ() + (dir[1] < 0 ? 0 : 4);
                    p1 = new BlockPos(cell.getX(), y, edgeZ);
                    p2 = new BlockPos(cell.getX() + 4, y, edgeZ);
                } else {
                    int edgeX = cell.getX() + (dir[0] < 0 ? 0 : 4);
                    p1 = new BlockPos(edgeX, y, cell.getZ());
                    p2 = new BlockPos(edgeX, y, cell.getZ() + 4);
                }
                walls.add(new WallEntry(p1, p2, color));
            }
        }
        return walls;
    }
}
