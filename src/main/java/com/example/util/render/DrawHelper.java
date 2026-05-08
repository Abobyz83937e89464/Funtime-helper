package com.example.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.Color;
import java.io.InputStream;
import java.nio.FloatBuffer;

public class DrawHelper {

    private static int progRect = -1;
    private static int progCard = -1;
    private static int vaoId    = -1;
    private static int vboId    = -1;

    private static final FloatBuffer VERT_BUF = BufferUtils.createFloatBuffer(20);
    private static final FloatBuffer MAT_BUF  = BufferUtils.createFloatBuffer(16);

    // ══════════════════════════════════════════════════════════════
    // INIT
    // ══════════════════════════════════════════════════════════════

    public static void init(ResourceManager rm) {
        if (progRect != -1) { GL20.glDeleteProgram(progRect);   progRect = -1; }
        if (progCard != -1) { GL20.glDeleteProgram(progCard);   progCard = -1; }
        if (vboId    != -1) { GL15.glDeleteBuffers(vboId);      vboId    = -1; }
        if (vaoId    != -1) { GL30.glDeleteVertexArrays(vaoId); vaoId    = -1; }

        try {
            String vsh1 = readResource(rm, "nocturn-client", "shaders/core/rounded_rect.vsh");
            String fsh1 = readResource(rm, "nocturn-client", "shaders/core/rounded_rect.fsh");
            if (vsh1 != null && fsh1 != null)
                System.out.println("[DrawHelper] rounded_rect OK id=" + (progRect = buildProgram(vsh1, fsh1)));

            String vsh2 = readResource(rm, "nocturn-client", "shaders/core/module_card.vsh");
            String fsh2 = readResource(rm, "nocturn-client", "shaders/core/module_card.fsh");
            if (vsh2 != null && fsh2 != null)
                System.out.println("[DrawHelper] module_card OK id=" + (progCard = buildProgram(vsh2, fsh2)));

            vaoId = GL30.glGenVertexArrays();
            vboId = GL15.glGenBuffers();

            GL30.glBindVertexArray(vaoId);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, 20L * Float.BYTES, GL15.GL_DYNAMIC_DRAW);

            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0L);
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3L * Float.BYTES);
            GL20.glEnableVertexAttribArray(1);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);

        } catch (Exception e) {
            System.err.println("[DrawHelper] init FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int buildProgram(String vsh, String fsh) {
        int vert = compileShader(GL20.GL_VERTEX_SHADER,   vsh);
        int frag = compileShader(GL20.GL_FRAGMENT_SHADER, fsh);
        int prog = GL20.glCreateProgram();
        GL20.glAttachShader(prog, vert);
        GL20.glAttachShader(prog, frag);
        GL20.glBindAttribLocation(prog, 0, "Position");
        GL20.glBindAttribLocation(prog, 1, "UV0");
        GL20.glLinkProgram(prog);
        GL20.glDeleteShader(vert);
        GL20.glDeleteShader(frag);
        if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            System.err.println("[DrawHelper] Link FAILED:\n" + GL20.glGetProgramInfoLog(prog));
            GL20.glDeleteProgram(prog);
            return -1;
        }
        return prog;
    }

    private static String readResource(ResourceManager rm, String ns, String path) {
        try {
            var opt = rm.getResource(Identifier.of(ns, path));
            if (opt.isEmpty()) { System.err.println("[DrawHelper] Not found: " + ns + ":" + path); return null; }
            try (InputStream is = opt.get().getInputStream()) { return new String(is.readAllBytes()); }
        } catch (Exception e) { System.err.println("[DrawHelper] readResource: " + e.getMessage()); return null; }
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE)
            System.err.println("[DrawHelper] Compile error:\n" + GL20.glGetShaderInfoLog(id));
        return id;
    }

    private static boolean vaoOk()  { return vaoId != -1 && vboId != -1; }
    private static boolean rectOk() { return progRect != -1 && vaoOk(); }
    private static boolean cardOk() { return progCard != -1 && vaoOk(); }

    // ══════════════════════════════════════════════════════════════
    // RESTORE STATE после нашего шейдера
    // ══════════════════════════════════════════════════════════════

    private static void restore() {
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager._glUseProgram(0);
        GlStateManager._disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // ПУБЛИЧНЫЕ МЕТОДЫ — RECT
    // ══════════════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                float radius, Color c) {
        sdf(m, x, y, w, h, radius, c, null, 0, null, 0, -1);
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h, Color c) {
        sdf(m, x, y, w, h, 0, c, null, 0, null, 0, -1);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color c) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, 0, c, null, 0, null, 0, -1);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h,
                                float radius, Color c) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, radius, c, null, 0, null, 0, -1);
    }

    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color c) {
        if (tl == tr && tr == br && br == bl) { drawRect(m, x, y, w, h, tl, c); return; }
        float topR = Math.max(tl, tr), botR = Math.max(bl, br), half = h / 2f;
        sdf(m, x, y,        w, half + 1, topR, c, null, 0, null, 0, -1);
        sdf(m, x, y + half, w, half + 1, botR, c, null, 0, null, 0, -1);
        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, c);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, c);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, c);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, c);
    }

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float radius, Color c, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2, h + thickness * 2,
            radius + thickness,
            new Color(0, 0, 0, 0), c, thickness, null, 0, -1);
    }

    // ✅ ВОЗВРАЩЁН — используется в CategoryElement и других местах
    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
                                float radius, float spread, Color color) {
        sdf(m,
            x - spread, y - spread,
            w + spread * 2f, h + spread * 2f,
            radius + spread,
            new Color(0, 0, 0, 0), null, 0, color, spread, -1);
    }

    public static void drawCircle(Matrix4f m, float cx, float cy, int seg, float r, Color c) {
        sdf(m, cx - r, cy - r, r * 2, r * 2, r, c, null, 0, null, 0, -1);
    }

    // ══════════════════════════════════════════════════════════════
    // MODULE CARD SHADER
    // ══════════════════════════════════════════════════════════════

    public static void drawModuleCard(Matrix4f mat,
                                      float x, float y, float w, float h,
                                      float radius,
                                      float idxOffset,
                                      float toggle,
                                      float hover,
                                      Color rainbowRGB) {
        if (!cardOk() || w <= 0 || h <= 0) return;

        float time = (System.currentTimeMillis() % 100_000L) / 1000f;

        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA.value,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value,
            GlStateManager.SrcFactor.ONE.value,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value);

        GlStateManager._glUseProgram(progCard);

        setMat4(progCard, "ModelViewMat", RenderSystem.getModelViewMatrix());
        setMat4(progCard, "ProjMat",      RenderSystem.getProjectionMatrix());
        u4f(progCard, "u_Rect",       0, 0, w, h);
        u1f(progCard, "u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u1f(progCard, "u_Time",       time);
        u1f(progCard, "u_Toggle",     toggle);
        u1f(progCard, "u_Hover",      hover);
        u1f(progCard, "u_IdxOffset",  idxOffset);
        u3f(progCard, "u_RainbowRGB",
            rainbowRGB.getRed()   / 255f,
            rainbowRGB.getGreen() / 255f,
            rainbowRGB.getBlue()  / 255f);

        uploadAndDraw(x, y, w, h);
        restore();
    }

    // ══════════════════════════════════════════════════════════════
    // ГРАДИЕНТЫ
    // ══════════════════════════════════════════════════════════════

    public static void drawGradientV(DrawContext ctx, float x, float y,
                                     float w, float h, Color top, Color bottom) {
        ctx.fillGradient((int)x, (int)y, (int)(x + w), (int)(y + h),
                         top.getRGB(), bottom.getRGB());
    }

    public static void drawGradientH(Matrix4f m, float x, float y, float w, float h,
                                     Color left, Color right) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,   y+h, 0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        buf.vertex(m, x+w, y+h, 0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x+w, y,   0).color(right.getRed(), right.getGreen(), right.getBlue(), right.getAlpha());
        buf.vertex(m, x,   y,   0).color(left.getRed(),  left.getGreen(),  left.getBlue(),  left.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // ТЕКСТ
    // ══════════════════════════════════════════════════════════════

    public static void drawText(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int)x, (int)y, c.getRGB(), false);
    }
    public static void drawTextBold(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int)x, (int)y, c.getRGB(), false);
    }
    public static void drawTextShadow(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.medium(text), (int)x, (int)y, c.getRGB(), true);
    }
    public static void drawTextBoldShadow(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.bold(text), (int)x, (int)y, c.getRGB(), true);
    }
    public static void drawTextRegular(DrawContext ctx, String text, float x, float y, Color c) {
        ctx.drawText(Fonts.get(), Fonts.regular(text), (int)x, (int)y, c.getRGB(), false);
    }

    // ══════════════════════════════════════════════════════════════
    // RAW RECT
    // ══════════════════════════════════════════════════════════════

    public static void drawRectRaw(Matrix4f m, float x, float y, float w, float h, Color c) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        float r = c.getRed()/255f, g = c.getGreen()/255f,
              b = c.getBlue()/255f, a = c.getAlpha()/255f;
        BufferBuilder buf = Tessellator.getInstance().begin(
            VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, x,   y+h, 0).color(r, g, b, a);
        buf.vertex(m, x+w, y+h, 0).color(r, g, b, a);
        buf.vertex(m, x+w, y,   0).color(r, g, b, a);
        buf.vertex(m, x,   y,   0).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // RAINBOW HELPER
    // ══════════════════════════════════════════════════════════════

    public static Color getRainbow(float offset, float speed, float sat,
                                   float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (long)(speed * 1000))
                     / (speed * 1000f) + offset) % 1f;
        Color c = Color.getHSBColor(hue, sat, brightness);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    // ══════════════════════════════════════════════════════════════
    // SDF CORE (rounded_rect shader)
    // ══════════════════════════════════════════════════════════════

    private static void sdf(Matrix4f mat,
                            float x, float y, float w, float h,
                            float radius,
                            Color fill,
                            Color outlineColor, float outlineWidth,
                            Color glowColor,    float glowRadius,
                            float time) {
        if (!rectOk() || w <= 0 || h <= 0) return;

        Color fc = fill         != null ? fill         : new Color(0, 0, 0, 0);
        Color oc = outlineColor != null ? outlineColor : new Color(0, 0, 0, 0);
        Color gc = glowColor    != null ? glowColor    : new Color(0, 0, 0, 0);

        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(
            GlStateManager.SrcFactor.SRC_ALPHA.value,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value,
            GlStateManager.SrcFactor.ONE.value,
            GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value);

        GlStateManager._glUseProgram(progRect);

        setMat4(progRect, "ModelViewMat", RenderSystem.getModelViewMatrix());
        setMat4(progRect, "ProjMat",      RenderSystem.getProjectionMatrix());

        u2f(progRect, "u_Resolution", w, h);
        u4f(progRect, "u_Rect",       0, 0, w, h);
        u1f(progRect, "u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u4f(progRect, "u_Color",
            fc.getRed()/255f, fc.getGreen()/255f, fc.getBlue()/255f, fc.getAlpha()/255f);
        u4f(progRect, "u_OutlineColor",
            oc.getRed()/255f, oc.getGreen()/255f, oc.getBlue()/255f, oc.getAlpha()/255f);
        u1f(progRect, "u_OutlineWidth", outlineColor != null ? outlineWidth : 0f);
        u4f(progRect, "u_GlowColor",
            gc.getRed()/255f, gc.getGreen()/255f, gc.getBlue()/255f, gc.getAlpha()/255f);
        u1f(progRect, "u_GlowRadius",   glowColor != null ? glowRadius : 0f);
        u1f(progRect, "u_Time",         time < 0 ? 0f : time);

        uploadAndDraw(x, y, w, h);
        restore();
    }

    // ══════════════════════════════════════════════════════════════
    // UPLOAD + DRAW
    // ══════════════════════════════════════════════════════════════

    private static void uploadAndDraw(float x, float y, float w, float h) {
        VERT_BUF.clear();
        VERT_BUF.put(x  ).put(y  ).put(0).put(0).put(0);
        VERT_BUF.put(x  ).put(y+h).put(0).put(0).put(1);
        VERT_BUF.put(x+w).put(y+h).put(0).put(1).put(1);
        VERT_BUF.put(x+w).put(y  ).put(0).put(1).put(0);
        VERT_BUF.flip();

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, VERT_BUF);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 4);
    }

    // ══════════════════════════════════════════════════════════════
    // GL HELPERS
    // ══════════════════════════════════════════════════════════════

    private static void u1f(int p, String n, float a) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform1f(l, a);
    }
    private static void u2f(int p, String n, float a, float b) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform2f(l, a, b);
    }
    private static void u3f(int p, String n, float a, float b, float c) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform3f(l, a, b, c);
    }
    private static void u4f(int p, String n, float a, float b, float c, float d) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform4f(l, a, b, c, d);
    }
    private static void setMat4(int p, String n, Matrix4f mat) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) {
            MAT_BUF.clear();
            mat.get(MAT_BUF);
            GL20.glUniformMatrix4fv(l, false, MAT_BUF);
        }
    }
}
