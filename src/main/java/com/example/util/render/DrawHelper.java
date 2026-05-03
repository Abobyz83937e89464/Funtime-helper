package com.example.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ── Rect ──────────────────────────────────────────────────
    public static void drawRect(MatrixStack ms, float x, float y, float w, float h, float r, Color color) {
        DrawContext ctx = new DrawContext(
            net.minecraft.client.MinecraftClient.getInstance(),
            net.minecraft.client.MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers()
        );
        drawRect(ms.peek().getPositionMatrix(), x, y, w, h, r, color);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        if (w <= 0 || h <= 0) return;
        ctx.fill((int)x, (int)y, (int)(x+w), (int)(y+h), color.getRGB());
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h, float r, Color color) {
        if (w <= 0 || h <= 0) return;
        drawRectRaw(m, x+r,   y,   w-r*2, h,     color);
        drawRectRaw(m, x,     y+r, r,     h-r*2, color);
        drawRectRaw(m, x+w-r, y+r, r,     h-r*2, color);
        drawCorner(m, x+r,   y+r,   r, 180, color);
        drawCorner(m, x+w-r, y+r,   r, 270, color);
        drawCorner(m, x+w-r, y+h-r, r, 0,   color);
        drawCorner(m, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color color) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m,x,   y+h,0).color(r,g,b,a);
        buf.vertex(m,x+w, y+h,0).color(r,g,b,a);
        buf.vertex(m,x+w, y,  0).color(r,g,b,a);
        buf.vertex(m,x,   y,  0).color(r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    private static void drawCorner(Matrix4f m, float cx, float cy, float r, float deg, Color color) {
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m,cx,cy,0).color(red,g,b,a);
        for(int i=0;i<=12;i++){double angle=Math.toRadians(deg+90.0*i/12);buf.vertex(m,cx+(float)Math.cos(angle)*r,cy+(float)Math.sin(angle)*r,0).color(red,g,b,a);}
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    // ── Outline ───────────────────────────────────────────────
    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h, float r, Color color, float thickness) {
        Matrix4f m = ms.peek().getPositionMatrix();
        for (float t = 0; t < thickness; t++) {
            drawOutlineRaw(m, x-t, y-t, w+t*2, h+t*2, r+t,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha()*(1-t/thickness))));
        }
    }

    private static void drawOutlineRaw(Matrix4f m, float x, float y, float w, float h, float r, Color color) {
        drawRectRaw(m, x+r,   y,     w-r*2, 1,     color);
        drawRectRaw(m, x+r,   y+h-1, w-r*2, 1,     color);
        drawRectRaw(m, x,     y+r,   1,     h-r*2, color);
        drawRectRaw(m, x+w-1, y+r,   1,     h-r*2, color);
        drawArc(m, x+r,   y+r,   r, 180, color);
        drawArc(m, x+w-r, y+r,   r, 270, color);
        drawArc(m, x+w-r, y+h-r, r, 0,   color);
        drawArc(m, x+r,   y+h-r, r, 90,  color);
    }

    private static void drawArc(Matrix4f m, float cx, float cy, float r, float deg, Color color) {
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for(int i=0;i<=12;i++){double angle=Math.toRadians(deg+90.0*i/12);float cos=(float)Math.cos(angle),sin=(float)Math.sin(angle);buf.vertex(m,cx+cos*(r-1),cy+sin*(r-1),0).color(red,g,b,a);buf.vertex(m,cx+cos*r,cy+sin*r,0).color(red,g,b,a);}
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    // ── Gradient ──────────────────────────────────────────────
    public static void drawGradientV(DrawContext ctx, float x, float y, float w, float h, Color top, Color bottom) {
        ctx.fillGradient((int)x,(int)y,(int)(x+w),(int)(y+h),top.getRGB(),bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h, Color left, Color right) {
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m,x,   y+h,0).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        buf.vertex(m,x+w, y+h,0).color(right.getRed(),right.getGreen(),right.getBlue(),right.getAlpha());
        buf.vertex(m,x+w, y,  0).color(right.getRed(),right.getGreen(),right.getBlue(),right.getAlpha());
        buf.vertex(m,x,   y,  0).color(left.getRed(), left.getGreen(), left.getBlue(), left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    // ── Styled Rect (скруглённые только нужные углы) ──────────
    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                       float tl, float tr, float br, float bl, Color color) {
        float maxR = Math.max(Math.max(tl,tr),Math.max(br,bl));
        drawRectRaw(m, x+tl, y,   w-tl-tr, h,   color);
        drawRectRaw(m, x,   y+tl, tl, h-tl-bl,  color);
        drawRectRaw(m, x+w-tr, y+tr, tr, h-tr-br, color);
        if (tl>0) drawCorner(m, x+tl,   y+tl,   tl, 180, color);
        if (tr>0) drawCorner(m, x+w-tr, y+tr,   tr, 270, color);
        if (br>0) drawCorner(m, x+w-br, y+h-br, br, 0,   color);
        if (bl>0) drawCorner(m, x+bl,   y+h-bl, bl, 90,  color);
    }

    // ── Circle ────────────────────────────────────────────────
    public static void drawCircle(Matrix4f m, float cx, float cy, int segments, float r, Color color) {
        RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float red=color.getRed()/255f,g=color.getGreen()/255f,b=color.getBlue()/255f,a=color.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        buf.vertex(m,cx,cy,0).color(red,g,b,a);
        for(int i=0;i<=segments;i++){double angle=Math.toRadians(360.0*i/segments);buf.vertex(m,cx+(float)Math.cos(angle)*r,cy+(float)Math.sin(angle)*r,0).color(red,g,b,a);}
        BufferRenderer.drawWithGlobalProgram(buf.end()); RenderSystem.disableBlend();
    }

    // ── Text ──────────────────────────────────────────────────
    public static void drawText(Matrix4f m, TextRenderer font, String text, float x, float y, Color color) {
        // Рисуем через MatrixStack-обёртку
        RenderSystem.enableBlend();
        net.minecraft.client.MinecraftClient.getInstance()
            .getTextRenderer().draw(text, x, y, color.getRGB(), false, m,
                net.minecraft.client.MinecraftClient.getInstance()
                    .getBufferBuilders().getEntityVertexConsumers(),
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        net.minecraft.client.MinecraftClient.getInstance()
            .getBufferBuilders().getEntityVertexConsumers().draw();
    }

    public static void drawText(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), text, (int)x, (int)y, color.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color color) {
        ctx.drawText(Fonts.get(), text, (int)x, (int)y, color.getRGB(), true);
    }
}
