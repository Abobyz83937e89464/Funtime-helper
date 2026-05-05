package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.Color;

public class DrawHelper {

    // ═══════════════════════════════════════════════════════
    // SHADER
    // ═══════════════════════════════════════════════════════

    private static ShaderProgram roundedShader;

    /**
     * Вызови в NocturnClient.onInitializeClient():
     * DrawHelper.registerShaders();
     */
    public static void registerShaders() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                Identifier.of("nocturn-client", "rounded_rect"),
                VertexFormats.POSITION_TEXTURE,
                prog -> roundedShader = prog
            );
        });
    }

    private static boolean ok() {
        return roundedShader != null;
    }

    // ═══════════════════════════════════════════════════════
    // DRAW RECT
    // ═══════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                Color color) {
        sdf(m, x, y, w, h, 0f, color, null, 0f, null, 0f, -1f);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h,
                                Color color) {
        drawRect(ctx.getMatrices().peek().getPositionMatrix(), x, y, w, h, 0f, color);
    }

    // ═══════════════════════════════════════════════════════
    // STYLED RECT (разные радиусы по углам)
    // ═══════════════════════════════════════════════════════

    /**
     * tl=верх-лево, tr=верх-право, br=низ-право, bl=низ-лево
     */
    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color color) {
        if (tl == tr && tr == br && br == bl) {
            drawRect(m, x, y, w, h, tl, color);
            return;
        }

        // Верхняя половина
        float topR = Math.max(tl, tr);
        float half = h / 2f;
        sdf(m, x, y, w, half + 1f, topR, color, null, 0f, null, 0f, -1f);

        // Нижняя половина
        float botR = Math.max(bl, br);
        sdf(m, x, y + half, w, half + 1f, botR, color, null, 0f, null, 0f, -1f);

        // Квадратные перекрытия для "отключённых" углов
        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, color);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, color);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, color);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, color);
    }

    // ═══════════════════════════════════════════════════════
    // OUTLINE
    // ═══════════════════════════════════════════════════════

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
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

    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
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
    // SHIMMER (радужный)
    // ═══════════════════════════════════════════════════════

    public static void drawShimmer(Matrix4f m, float x, float y, float w, float h,
                                   float radius, Color baseColor) {
        float time = (System.currentTimeMillis() % 100_000L) / 1000f;
        sdf(m, x, y, w, h, radius, baseColor, null, 0f, null, 0f, time);
    }

    // ═══════════════════════════════════════════════════════
    // CIRCLE
    // ═══════════════════════════════════════════════════════

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments,
                                  float r, Color color) {
        // segments игнорируем — SDF даёт идеальный круг
        sdf(m, cx - r, cy - r, r * 2f, r * 2f, r, color, null, 0f, null, 0f, -1f);
    }

    // ═══════════════════════════════════════════════════════
    // RAINBOW
    // ═══════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed, float sat,
                                   float brightness, int alpha) {
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
            (int) x, (int) y, (int)(x + w), (int)(y + h),
            top.getRGB(), bottom.getRGB()
        );
    }

    public static void drawGradientH(Matrix4f m,
                                     float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buf.vertex(m, x,     y + h, 0)
           .color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x + w, y + h, 0)
           .color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x + w, y,     0)
           .color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,     y,     0)
           .color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());

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
    // RAW RECT (без шейдера, для разделителей 1px)
    // ═══════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m,
                                   float x, float y, float w, float h,
                                   Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

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
        if (!ok() || w <= 0 || h <= 0) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SrcFactor.ONE,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShader(() -> roundedShader);

        // ── Юниформы ──────────────────────────────────────────────
        setUniform2f("u_Resolution", w, h);
        setUniform4f("u_Rect", 0f, 0f, w, h);
        setUniform1f("u_Radius", Math.min(radius, Math.min(w, h) / 2f));

        Color fc = fill         != null ? fill         : new Color(0, 0, 0, 0);
        Color oc = outlineColor != null ? outlineColor : new Color(0, 0, 0, 0);
        Color gc = glowColor    != null ? glowColor    : new Color(0, 0, 0, 0);

        setUniform4f("u_Color",
            fc.getRed() / 255f, fc.getGreen() / 255f,
            fc.getBlue() / 255f, fc.getAlpha() / 255f);
        setUniform4f("u_OutlineColor",
            oc.getRed() / 255f, oc.getGreen() / 255f,
            oc.getBlue() / 255f, oc.getAlpha() / 255f);
        setUniform1f("u_OutlineWidth", outlineColor != null ? outlineWidth : 0f);
        setUniform4f("u_GlowColor",
            gc.getRed() / 255f, gc.getGreen() / 255f,
            gc.getBlue() / 255f, gc.getAlpha() / 255f);
        setUniform1f("u_GlowRadius",  glowColor != null ? glowRadius : 0f);
        setUniform1f("u_Time",        time < 0f ? 0f : time);

        roundedShader.bind();

        // ── Геометрия ─────────────────────────────────────────────
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        buf.vertex(mat, x,     y,     0).texture(0f, 0f);
        buf.vertex(mat, x,     y + h, 0).texture(0f, 1f);
        buf.vertex(mat, x + w, y + h, 0).texture(1f, 1f);
        buf.vertex(mat, x + w, y,     0).texture(1f, 0f);

        BufferRenderer.drawWithGlobalProgram(buf.end());

        roundedShader.unbind();
        RenderSystem.disableBlend();
    }

    // ═══════════════════════════════════════════════════════
    // UNIFORM HELPERS
    // ═══════════════════════════════════════════════════════

    private static void setUniform1f(String name, float a) {
        var u = roundedShader.getUniformOrDefault(name);
        if (u != null) u.set(a);
    }

    private static void setUniform2f(String name, float a, float b) {
        var u = roundedShader.getUniformOrDefault(name);
        if (u != null) u.set(a, b);
    }

    private static void setUniform4f(String name, float a, float b, float c, float d) {
        var u = roundedShader.getUniformOrDefault(name);
        if (u != null) u.set(a, b, c, d);
    }
}
