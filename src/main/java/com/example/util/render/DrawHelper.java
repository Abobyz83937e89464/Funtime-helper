package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), color.getRGB());
    }

    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h, Color top, Color bottom) {
        if (w <= 0 || h <= 0) return;
        ctx.fillGradient((int)x, (int)y, (int)(x+w), (int)(y+h), top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(DrawContext ctx, float x, float y, float w, float h, Color left, Color right) {
        if (w <= 0 || h <= 0) return;
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m,x,   y+h,0).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        buf.vertex(m,x+w, y+h,0).color(right.getRed(),right.getGreen(),right.getBlue(),right.getAlpha());
        buf.vertex(m,x+w, y,  0).color(right.getRed(),right.getGreen(),right.getBlue(),right.getAlpha());
        buf.vertex(m,x,   y,  0).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(DrawContext ctx, float x, float y, float w, float h, float r, Color color) {
        if (r <= 0) { drawRect(ctx, x, y, w, h, color); return; }
        drawRect(ctx, x+r,   y,   w-r*2, h,     color);
        drawRect(ctx, x,     y+r, r,     h-r*2, color);
        drawRect(ctx, x+w-r, y+r, r,     h-r*2, color);
        drawCorner(ctx, x+r,   y+r,   r, 180, color);
        drawCorner(ctx, x+w-r, y+r,   r, 270, color);
        drawCorner(ctx, x+w-r, y+h-r, r, 0,   color);
        drawCorner(ctx, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawCorner(DrawContext ctx, float cx, float cy, float r, float deg, Color color) {
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m,cx,cy,0).color(red,g,b,a);
        for(int i=0;i<=12;i++){double angle=Math.toRadians(deg+90.0*i/12);buf.vertex(m,cx+(float)Math.cos(angle)*r,cy+(float)Math.sin(angle)*r,0).color(red,g,b,a);}
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    public static void drawOutline(DrawContext ctx, float x, float y, float w, float h, float r, Color color) {
        drawRect(ctx, x+r,   y,     w-r*2, 1,     color);
        drawRect(ctx, x+r,   y+h-1, w-r*2, 1,     color);
        drawRect(ctx, x,     y+r,   1,     h-r*2, color);
        drawRect(ctx, x+w-1, y+r,   1,     h-r*2, color);
        drawArc(ctx, x+r,   y+r,   r, 180, color);
        drawArc(ctx, x+w-r, y+r,   r, 270, color);
        drawArc(ctx, x+w-r, y+h-r, r, 0,   color);
        drawArc(ctx, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawArc(DrawContext ctx, float cx, float cy, float r, float deg, Color color) {
        Matrix4f m = ctx.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for(int i=0;i<=12;i++){double angle=Math.toRadians(deg+90.0*i/12);float cos=(float)Math.cos(angle),sin=(float)Math.sin(angle);buf.vertex(m,cx+cos*(r-1),cy+sin*(r-1),0).color(red,g,b,a);buf.vertex(m,cx+cos*r,cy+sin*r,0).color(red,g,b,a);}
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    // Обычный текст (0.9x — чуть компактнее)
    public static void text(DrawContext ctx, String t, float x, float y, Color c) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(0.9f, 0.9f, 1f);
        ctx.drawText(Fonts.get(), t, 0, 0, c.getRGB(), false);
        ctx.getMatrices().pop();
    }

    // Текст с тенью — крупнее (1.1x — для заголовков)
    public static void textShadow(DrawContext ctx, String t, float x, float y, Color c) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(1.1f, 1.1f, 1f);
        ctx.drawText(Fonts.get(), t, 0, 0, c.getRGB(), true);
        ctx.getMatrices().pop();
    }

    // Крупный жирный текст (1.3x — лого и названия модулей)
    public static void textBig(DrawContext ctx, String t, float x, float y, Color c) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(1.3f, 1.3f, 1f);
        ctx.drawText(Fonts.get(), t, 0, 0, c.getRGB(), true);
        ctx.getMatrices().pop();
    }

    // Ширина с учётом scale
    public static int tw(String t)     { return (int)(Fonts.get().getWidth(t) * 0.9f); }
    public static int twBig(String t)  { return (int)(Fonts.get().getWidth(t) * 1.3f); }
    public static int twHdr(String t)  { return (int)(Fonts.get().getWidth(t) * 1.1f); }
    public static int th()             { return (int)(Fonts.get().fontHeight   * 0.9f); }
    public static int thBig()          { return (int)(Fonts.get().fontHeight   * 1.3f); }
}
