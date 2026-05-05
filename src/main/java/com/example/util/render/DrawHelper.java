package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.List;

public class DrawHelper {

    // ═══════════════════════════════════════════════════════
    // SHADER KEY — регистрация кастомного шейдера
    // ═══════════════════════════════════════════════════════

    public static final ShaderProgramKey ROUNDED = new ShaderProgramKey(
        Identifier.of("nocturn-client", "rounded_rect"),
        VertexFormats.POSITION_TEXTURE,
        List.of(
            new ShaderProgramKey.Uniform("u_Resolution",   GlUniform.Type.FLOAT_2, 2,  new float[]{1920f, 1080f}),
            new ShaderProgramKey.Uniform("u_Rect",         GlUniform.Type.FLOAT_4, 4,  new float[]{0,0,100,100}),
            new ShaderProgramKey.Uniform("u_Radius",       GlUniform.Type.FLOAT,   1,  new float[]{8f}),
            new ShaderProgramKey.Uniform("u_Color",        GlUniform.Type.FLOAT_4, 4,  new float[]{1,1,1,1}),
            new ShaderProgramKey.Uniform("u_OutlineColor", GlUniform.Type.FLOAT_4, 4,  new float[]{0,0,0,0}),
            new ShaderProgramKey.Uniform("u_OutlineWidth", GlUniform.Type.FLOAT,   1,  new float[]{0f}),
            new ShaderProgramKey.Uniform("u_GlowColor",    GlUniform.Type.FLOAT_4, 4,  new float[]{0,0,0,0}),
            new ShaderProgramKey.Uniform("u_GlowRadius",   GlUniform.Type.FLOAT,   1,  new float[]{0f}),
            new ShaderProgramKey.Uniform("u_Time",         GlUniform.Type.FLOAT,   1,  new float[]{0f})
        )
    );

    // ═══════════════════════════════════════════════════════
    // SHADER HELPERS
    // ═══════════════════════════════════════════════════════

    private static ShaderProgram getShader() {
        return MinecraftClient.getInstance()
                              .gameRenderer
                              .getProgram(ROUNDED);
    }

    private static boolean ok() {
        return getShader() != null;
    }

    // ═══════════════════════════════════════════════════════
    // DRAW RECT
    // ═══════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y,
                                float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(Matrix4f m, float x, float y,
                                float w, float h, Color color) {
        sdf(m, x, y, w, h, 0f, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(DrawContext ctx, float x, float y,
                                float w, float h, Color color) {
        drawRect(ctx.getMatrices().peek().getPositionMatrix(),
                 x, y, w, h, 0f, color);
    }

    // ═══════════════════════════════════════════════════════
    // STYLED RECT (разные радиусы углов)
    // ═══════════════════════════════════════════════════════

    public static void drawStyledRect(Matrix4f m,
                                      float x, float y, float w, float h,
                                      float tl, float tr,
                                      float br, float bl,
                                      Color color) {
        if (tl == tr && tr == br && br == bl) {
            drawRect(m, x, y, w, h, tl, color);
            return;
        }
        float topR = Math.max(tl, tr);
        float botR = Math.max(bl, br);
        float half = h / 2f;

        sdf(m, x, y,        w, half + 1f, topR, color, null, 0f, null, 0f, -1f);
        sdf(m, x, y + half, w, half + 1f, botR, color, null, 0f, null, 0f, -1f);

        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, color);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, color);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, color);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, color);
    }

    // ═══════════════════════════════════════════════════════
    // OUTLINE
    // ═══════════════════════════════════════════════════════

    public static void drawOutline(MatrixStack ms,
                                   float x, float y, float w, float h,
                                   float radius, Color color, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2f, h + thickness * 2f,
            radius + thickness,
            new Color(0, 0, 0, 0),
            color, thickness,
            null, 0f, -1f);
    }

    // ═══════════════════════════════════════════════════════
    // GLOW
    // ═══════════════════════════════════════════════════════

    public static void drawGlow(Matrix4f m,
                                float x, float y, float w, float h,
                                float radius, float spread, Color color) {
        sdf(m,
            x - spread, y - spread,
            w + spread * 2f, h + spread * 2f,
            radius + spread,
            new Color(0, 0, 0, 0),
            null, 0f,
            color, spread,
            -1f);
    }

    // ═══════════════════════════════════════════════════════
    // SHIMMER
    // ═══════════════════════════════════════════════════════

    public static void drawShimmer(Matrix4f m,
                                   float x, float y, float w, float h,
                                   float radius, Color baseColor) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000f;
        sdf(m, x, y, w, h, radius, baseColor, null, 0f, null, 0f, time);
    }

    // ═══════════════════════════════════════════════════════
    // CIRCLE
    // ═══════════════════════════════════════════════════════

    public static void drawCircle(Matrix4f m,
                                  float cx, float cy,
                                  int segments, float r, Color color) {
        sdf(m, cx - r, cy - r, r * 2f, r * 2f, r,
            color, null, 0f, null, 0f, -1f);
    }

    // ═══════════════════════════════════════════════════════
    // RAINBOW
    // ═══════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed,
                                   float sat, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000f))
                     / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, sat, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ═══════════════════════════════════════════════════════
    // GRADIENTS
    // ═══════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx,
                                     float x, float y, float w, float h,
                                     Color top, Color bottom) {
        ctx.fillGradient(
            (int) x, (int) y,
            (int)(x + w), (int)(y + h),
            top.getRGB(), bottom.getRGB()
        );
    }

    public static void drawGradientH(Matrix4f m,
                                     float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0)
           .color(left.getRed(),  left.getGreen(),
                  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x + w, y + h, 0)
           .color(right.getRed(), right.getGreen(),
                  right.getBlue(), right.getAlpha());
        buf.vertex(m, x + w, y,     0)
           .color(right.getRed(), right.getGreen(),
                  right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,     0)
           .color(left.getRed(),  left.getGreen(),
                  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════
    // TEXT
    // ═══════════════════════════════════════════════════════

    public static void drawText(DrawContext ctx, String text,
                                float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    public static void drawTextBold(DrawContext ctx, String text,
                                    float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    public static void drawTextShadow(DrawContext ctx, String text,
                                      float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text),
                     (int) x, (int) y, c.getRGB(), true);
    }

    public static void drawTextBoldShadow(DrawContext ctx, String text,
                                          float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text),
                     (int) x, (int) y, c.getRGB(), true);
    }

    public static void drawTextRegular(DrawContext ctx, String text,
                                       float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.regular(text),
                     (int) x, (int) y, c.getRGB(), false);
    }

    // ═══════════════════════════════════════════════════════
    // RAW RECT
    // ═══════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m,
                                   float x, float y, float w, float h,
                                   Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float r = c.getRed()   / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue()  / 255f;
        float a = c.getAlpha() / 255f;

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y + h, 0).color(r, g, b, a);
        buf.vertex(m, x + w, y,     0).color(r, g, b, a);
        buf.vertex(m, x,     y,     0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════
    // CORE SDF
    // ═══════════════════════════════════════════════════════

    private static void sdf(Matrix4f mat,
                             float x, float y, float w, float h,
                             float radius,
                             Color fill,
                             Color outlineColor, float outlineWidth,
                             Color glowColor,    float glowRadius,
                             float time) {
        if (w <= 0 || h <= 0) return;

        // Если шейдер не загрузился — fallback на простой прямоугольник
        if (!ok()) {
            if (fill != null && fill.getAlpha() > 0)
                drawRectRaw(mat, x, y, w, h, fill);
            return;
        }

        ShaderProgram shader = getShader();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShader(ROUNDED);

        // Uniforms
        Color fc = fill         != null ? fill         : new Color(0, 0, 0, 0);
        Color oc = outlineColor != null ? outlineColor : new Color(0, 0, 0, 0);
        Color gc = glowColor    != null ? glowColor    : new Color(0, 0, 0, 0);

        setU2f(shader, "u_Resolution", w, h);
        setU4f(shader, "u_Rect",       0f, 0f, w, h);
        setU1f(shader, "u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        setU4f(shader, "u_Color",
            fc.getRed()/255f, fc.getGreen()/255f,
            fc.getBlue()/255f, fc.getAlpha()/255f);
        setU4f(shader, "u_OutlineColor",
            oc.getRed()/255f, oc.getGreen()/255f,
            oc.getBlue()/255f, oc.getAlpha()/255f);
        setU1f(shader, "u_OutlineWidth",
            outlineColor != null ? outlineWidth : 0f);
        setU4f(shader, "u_GlowColor",
            gc.getRed()/255f, gc.getGreen()/255f,
            gc.getBlue()/255f, gc.getAlpha()/255f);
        setU1f(shader, "u_GlowRadius",
            glowColor != null ? glowRadius : 0f);
        setU1f(shader, "u_Time", time < 0f ? 0f : time);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buf.vertex(mat, x,     y,     0).texture(0f, 0f);
        buf.vertex(mat, x,     y + h, 0).texture(0f, 1f);
        buf.vertex(mat, x + w, y + h, 0).texture(1f, 1f);
        buf.vertex(mat, x + w, y,     0).texture(1f, 0f);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════
    // UNIFORM HELPERS
    // ═══════════════════════════════════════════════════════

    private static void setU1f(ShaderProgram s, String name, float a) {
        GlUniform u = s.getUniform(name);
        if (u != null) u.set(a);
    }

    private static void setU2f(ShaderProgram s, String name,
                                float a, float b) {
        GlUniform u = s.getUniform(name);
        if (u != null) u.set(a, b);
    }

    private static void setU4f(ShaderProgram s, String name,
                                float a, float b, float c, float d) {
        GlUniform u = s.getUniform(name);
        if (u != null) u.set(a, b, c, d);
    }
                         }
