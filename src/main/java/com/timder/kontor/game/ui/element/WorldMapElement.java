package com.timder.kontor.game.ui.element;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector2f;

public class WorldMapElement extends UIElement {

    private static final int GRID_COLOR = 0x40FFFFFF;
    private static final int SELECTION_FILL_COLOR = 0x60FFAA00;
    private static final int SELECTION_BORDER_COLOR = 0xFFFFAA00;

    private static final int MAX_BLOCKS = 1024;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 8f;

    private float zoom = 1f;
    private final int radiusChunks;
    private int width;
    private int height;
    private int centerX;
    private int centerZ;
    private boolean dirty = true;
    private int lastLoadedChunks = -1;
    private int sliceY;

    @Nullable
    private ChunkPos selectedChunk;

    private Consumer<ChunkPos> selectionListener = chunk -> {};
    private ToIntFunction<ChunkPos> chunkColor = chunk -> 0;

    private DynamicTexture texture;
    private ResourceLocation textureId;

    public WorldMapElement(int radiusChunks) {
        if (radiusChunks < 0) throw new IllegalArgumentException("radiusChunks must not be negative.");
        this.radiusChunks = radiusChunks;

        addEventListener(UIEvents.MOUSE_WHEEL, event -> {
            zoom = Mth.clamp(event.deltaY > 0 ? zoom * 2 : zoom / 2, MIN_ZOOM, MAX_ZOOM);
            event.stopPropagation();
        });
        addEventListener(UIEvents.MOUSE_DOWN, this::onMouseDown);
    }

    public WorldMapElement setCenter(int blockX, int blockZ) {
        centerX = blockX;
        centerZ = blockZ;
        dirty = true;
        return this;
    }

    public WorldMapElement onSelectedChunkChanged(Consumer<ChunkPos> listener) {
        selectionListener = listener;
        return this;
    }

    public WorldMapElement setChunkColor(ToIntFunction<ChunkPos> function) {
        chunkColor = function;
        return this;
    }

    public WorldMapElement setSliceY(int blockY) {
        sliceY = blockY;
        dirty = true;
        return this;
    }

    @Nullable
    public ChunkPos getSelectedChunk() {
        return selectedChunk;
    }

    public WorldMapElement setSelectedChunk(@Nullable ChunkPos chunk) {
        selectedChunk = chunk;
        return this;
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();
        freeTexture();
    }

    private void freeTexture() {
        if (textureId != null) {
            Minecraft.getInstance().getTextureManager().release(textureId);
            texture = null;
            textureId = null;
        }
    }

    private boolean inRange(int x, int z) {
        return Math.abs((x >> 4) - (centerX >> 4)) <= radiusChunks
                && Math.abs((z >> 4) - (centerZ >> 4)) <= radiusChunks;
    }

    @Override
    public void drawBackgroundAdditional(GUIContext ctx) {
        super.drawBackgroundAdditional(ctx);

        int drawWidth = Math.round(getContentWidth());
        int drawHeight = Math.round(getContentHeight());
        int w = Mth.clamp(Math.round(drawWidth / zoom), 1, MAX_BLOCKS);
        int h = Mth.clamp(Math.round(drawHeight / zoom), 1, MAX_BLOCKS);
        if (texture == null || w != width || h != height) {
            freeTexture();
            width = w;
            height = h;
            texture = new DynamicTexture(new NativeImage(w, h, true));
            textureId = Minecraft.getInstance().getTextureManager().register("createkontor_world_map", texture);
            dirty = true;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            int loaded = loadedChunks(level);
            if (loaded != lastLoadedChunks) {
                lastLoadedChunks = loaded;
                dirty = true;
            }
        }
        if (dirty) {
            dirty = false;
            paint();
        }

        GuiGraphics g = ctx.graphics;
        g.flush();
        g.blit(
                textureId,
                Math.round(getContentX()),
                Math.round(getContentY()),
                drawWidth,
                drawHeight,
                0,
                0,
                w,
                h,
                w,
                h);
        drawChunkColors(g, drawWidth, drawHeight);
        drawGrid(g, drawWidth, drawHeight);
        drawSelection(g, drawWidth, drawHeight);
    }

    private record Surface(MapColor color, int y, int waterDepth) {}

    private void paint() {
        ClientLevel level = Minecraft.getInstance().level;
        NativeImage image = texture.getPixels();
        if (level == null || image == null) {
            return;
        }
        int minX = centerX - width / 2;
        int minZ = centerZ - height / 2;

        for (int px = 0; px < width; px++) {
            Surface north = surface(level, minX + px, minZ - 1, sliceY);
            double previous = north == null ? 0.0 : north.y();

            for (int pz = 0; pz < height; pz++) {
                Surface surface = surface(level, minX + px, minZ + pz, sliceY);
                if (surface == null) {
                    image.setPixelRGBA(px, pz, 0);
                    continue;
                }
                double height = surface.y();
                int parity = (px + pz) & 1;

                MapColor.Brightness brightness;
                if (surface.color() == MapColor.WATER) {
                    double depth = surface.waterDepth() * 0.1 + parity * 0.2;
                    brightness = depth < 0.5
                            ? MapColor.Brightness.HIGH
                            : depth > 0.9 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                } else {
                    double slope = (height - previous) + (parity - 0.5) * 0.4;
                    brightness = slope > 0.6
                            ? MapColor.Brightness.HIGH
                            : slope < -0.6 ? MapColor.Brightness.LOW : MapColor.Brightness.NORMAL;
                }
                previous = height;
                int color = MapColor.getColorFromPackedId(surface.color().getPackedId(brightness));
                image.setPixelRGBA(px, pz, inRange(minX + px, minZ + pz) ? color : 0);
            }
        }
        texture.upload();
    }

    private void drawChunkColors(GuiGraphics g, int drawWidth, int drawHeight) {
        float scaleX = (float) drawWidth / width;
        float scaleZ = (float) drawHeight / height;
        int minX = centerX - width / 2;
        int minZ = centerZ - height / 2;
        int firstX = Math.max(minX >> 4, (centerX >> 4) - radiusChunks);
        int lastX = Math.min((minX + width - 1) >> 4, (centerX >> 4) + radiusChunks);
        int firstZ = Math.max(minZ >> 4, (centerZ >> 4) - radiusChunks);
        int lastZ = Math.min((minZ + height - 1) >> 4, (centerZ >> 4) + radiusChunks);

        for (int cx = firstX; cx <= lastX; cx++) {
            for (int cz = firstZ; cz <= lastZ; cz++) {
                int color = chunkColor.applyAsInt(new ChunkPos(cx, cz));
                if ((color >>> 24) == 0) {
                    continue;
                }
                float x0 = Math.max((cx * 16 - minX) * scaleX, 0);
                float x1 = Math.min(((cx + 1) * 16 - minX) * scaleX, drawWidth);
                float z0 = Math.max((cz * 16 - minZ) * scaleZ, 0);
                float z1 = Math.min(((cz + 1) * 16 - minZ) * scaleZ, drawHeight);
                DrawerHelper.drawSolidRect(g, getContentX() + x0, getContentY() + z0, x1 - x0, z1 - z0, color);
            }
        }
    }

    private void drawGrid(GuiGraphics g, int drawWidth, int drawHeight) {
        float scaleX = (float) drawWidth / width;
        float scaleZ = (float) drawHeight / height;
        int minX = centerX - width / 2;
        int minZ = centerZ - height / 2;
        int fromX = ((centerX >> 4) - radiusChunks) * 16;
        int toX = ((centerX >> 4) + radiusChunks + 1) * 16;
        int fromZ = ((centerZ >> 4) - radiusChunks) * 16;
        int toZ = ((centerZ >> 4) + radiusChunks + 1) * 16;

        float left = Mth.clamp((fromX - minX) * scaleX, 0, drawWidth);
        float right = Mth.clamp((toX - minX) * scaleX, 0, drawWidth);
        float top = Mth.clamp((fromZ - minZ) * scaleZ, 0, drawHeight);
        float bottom = Mth.clamp((toZ - minZ) * scaleZ, 0, drawHeight);

        for (int x = fromX; x <= toX; x += 16) {
            float offset = (x - minX) * scaleX;
            if (offset >= 0 && offset < drawWidth) {
                DrawerHelper.drawSolidRect(
                        g, getContentX() + offset, getContentY() + top, 1f, bottom - top, GRID_COLOR);
            }
        }
        for (int z = fromZ; z <= toZ; z += 16) {
            float offset = (z - minZ) * scaleZ;
            if (offset >= 0 && offset < drawHeight) {
                DrawerHelper.drawSolidRect(
                        g, getContentX() + left, getContentY() + offset, right - left, 1f, GRID_COLOR);
            }
        }
    }

    private void drawSelection(GuiGraphics g, int drawWidth, int drawHeight) {
        if (selectedChunk == null) {
            return;
        }
        float scaleX = (float) drawWidth / width;
        float scaleZ = (float) drawHeight / height;
        float x0 = (selectedChunk.getMinBlockX() - (centerX - width / 2)) * scaleX;
        float z0 = (selectedChunk.getMinBlockZ() - (centerZ - height / 2)) * scaleZ;
        float x1 = x0 + 16 * scaleX;
        float z1 = z0 + 16 * scaleZ;
        if (x1 <= 0 || z1 <= 0 || x0 >= drawWidth || z0 >= drawHeight) {
            return;
        }
        float left = Math.max(x0, 0) + getContentX();
        float top = Math.max(z0, 0) + getContentY();
        float right = Math.min(x1, drawWidth) + getContentX();
        float bottom = Math.min(z1, drawHeight) + getContentY();

        DrawerHelper.drawSolidRect(g, left, top, right - left, bottom - top, SELECTION_FILL_COLOR);
        if (x0 >= 0) DrawerHelper.drawSolidRect(g, left, top, 1f, bottom - top, SELECTION_BORDER_COLOR);
        if (x1 <= drawWidth) DrawerHelper.drawSolidRect(g, right, top, 1f, bottom - top, SELECTION_BORDER_COLOR);
        if (z0 >= 0) DrawerHelper.drawSolidRect(g, left, top, right - left, 1f, SELECTION_BORDER_COLOR);
        if (z1 <= drawHeight)
            DrawerHelper.drawSolidRect(g, left, bottom, right - left + 1f, 1f, SELECTION_BORDER_COLOR);
    }

    private int loadedChunks(ClientLevel level) {
        int minX = centerX - width / 2;
        int minZ = centerZ - height / 2;
        int count = 0;
        for (int cx = minX >> 4; cx <= (minX + width - 1) >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= (minZ + height - 1) >> 4; cz++) {
                if (inRange(cx << 4, cz << 4) && level.getChunkSource().getChunkNow(cx, cz) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    private static Surface surface(ClientLevel level, int x, int z, int sliceY) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(x >> 4, z >> 4);
        if (chunk == null || chunk.isEmpty()) {
            return null;
        }

        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, 0, z);
        int y = level.dimensionType().hasCeiling()
                ? Mth.clamp(sliceY + 1, minY + 1, level.getMaxBuildHeight())
                : chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        int waterDepth = 0;
        BlockState state;

        if (y > minY + 1) {
            do {
                y--;
                pos.setY(y);
                state = chunk.getBlockState(pos);
            } while (state.getMapColor(level, pos) == MapColor.NONE && y > minY);

            if (y > minY && !state.getFluidState().isEmpty()) {
                BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos(x, 0, z);
                int belowY = y - 1;
                BlockState belowState;
                do {
                    below.setY(belowY--);
                    belowState = chunk.getBlockState(below);
                    waterDepth++;
                } while (belowY > minY && !belowState.getFluidState().isEmpty());

                FluidState fluid = state.getFluidState();
                if (!state.isFaceSturdy(level, pos, Direction.UP)) {
                    state = fluid.createLegacyBlock();
                }
            }
        } else {
            state = Blocks.BEDROCK.defaultBlockState();
        }
        return new Surface(state.getMapColor(level, pos), y, waterDepth);
    }

    private void onMouseDown(UIEvent event) {
        int drawWidth = Math.round(getContentWidth());
        int drawHeight = Math.round(getContentHeight());

        if (event.button != 0 || event.target != this || width == 0 || drawWidth == 0 || drawHeight == 0) {
            return;
        }

        Vector2f mouse = getLocalMouse(event.x, event.y);
        float offsetX = mouse.x - getContentX();
        float offsetZ = mouse.y - getContentY();
        if (offsetX < 0 || offsetZ < 0 || offsetX >= drawWidth || offsetZ >= drawHeight) {
            return;
        }

        int blockX = centerX - width / 2 + (int) (offsetX / drawWidth * width);
        int blockZ = centerZ - height / 2 + (int) (offsetZ / drawHeight * height);
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null
                || !inRange(blockX, blockZ)
                || level.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4) == null) {
            return;
        }
        ChunkPos clicked = new ChunkPos(blockX >> 4, blockZ >> 4);
        if (!clicked.equals(selectedChunk)) {
            selectedChunk = clicked;
            selectionListener.accept(clicked);
        }
    }
}
