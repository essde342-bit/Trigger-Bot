package kronex.fun.other.utils.display.shape;

import net.minecraft.client.util.math.MatrixStack;

public final class ShapeProperties {
    public final MatrixStack matrix;
    public final float x;
    public final float y;
    public final float width;
    public final float height;
    public int color = 0xFFFFFFFF;
    public int outlineColor = 0;
    public float thickness = 0;
    public float radius = 0;
    public float softness = 0;
    public int quality = 1;

    private ShapeProperties(MatrixStack matrix, float x, float y, float width, float height) {
        this.matrix = matrix;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static ShapeProperties create(MatrixStack matrix, float x, float y, float width, float height) {
        return new ShapeProperties(matrix, x, y, width, height);
    }

    public ShapeProperties round(float radius) {
        this.radius = radius;
        return this;
    }

    public ShapeProperties round(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.radius = Math.max(Math.max(topLeft, topRight), Math.max(bottomRight, bottomLeft));
        return this;
    }

    public ShapeProperties thickness(float thickness) {
        this.thickness = thickness;
        return this;
    }

    public ShapeProperties softness(float softness) {
        this.softness = softness;
        return this;
    }

    public ShapeProperties quality(int quality) {
        this.quality = quality;
        return this;
    }

    public ShapeProperties outlineColor(int color) {
        this.outlineColor = color;
        return this;
    }

    public ShapeProperties color(int color) {
        this.color = color;
        return this;
    }

    public ShapeProperties color(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        this.color = average(topLeft, topRight, bottomLeft, bottomRight);
        return this;
    }

    public ShapeProperties color(Object ignored) {
        return this;
    }

    public ShapeProperties build() {
        return this;
    }

    private static int average(int a, int b, int c, int d) {
        int aa = ((a >>> 24) + (b >>> 24) + (c >>> 24) + (d >>> 24)) / 4;
        int rr = (((a >>> 16) & 0xFF) + ((b >>> 16) & 0xFF) + ((c >>> 16) & 0xFF) + ((d >>> 16) & 0xFF)) / 4;
        int gg = (((a >>> 8) & 0xFF) + ((b >>> 8) & 0xFF) + ((c >>> 8) & 0xFF) + ((d >>> 8) & 0xFF)) / 4;
        int bb = ((a & 0xFF) + (b & 0xFF) + (c & 0xFF) + (d & 0xFF)) / 4;
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }
}