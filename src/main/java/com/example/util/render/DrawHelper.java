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

    // ══════════════════════════════════════════════════════════════
    // SHADER 1: rounded_rect  (фон меню, outline, glow)
    // SHADER 2: module_card   (карточки с переливанием)
    // ══════════════════════════════════════════════════════════════

    private static int progRect = -1;  // rounded_rect
    private static int progCard = -1;  // module_card

    private static int vaoId = -1;
    private static int vboId = -1;

    private static final FloatBuffer VERT_BUF = BufferUtils.createFloatBuffer(20);
    private static final FloatBuffer MAT_BUF  = BufferUtils.createFloatBuffer(16);

    // ══════════════════════════════════════════════════════════════
    // INIT
    // ══════════════════════════════════════════════════════════════

    public static void init(ResourceManager rm) {
        // Удаляем старые программы
        if (progRect != -1) { GL20.glDeleteProgram(progRect); progRect = -1; }
        if (progCard != -1) { GL20.glDeleteProgram(progCard); progCard = -1; }
        if (vboId    != -1) { GL15.glDeleteBuffers(vboId);    vboId    = -1; }
        if (vaoId    != -1) { GL30.glDeleteVertexArrays(vaoId); vaoId  = -1; }

        try {
            // Шейдер 1
            String vsh1 = readResource(rm, "nocturn-client", "shaders/core/rounded_rect.vsh");
            String fsh1 = readResource(rm, "nocturn-client", "shaders/core/rounded_rect.fsh");
            if (vsh1 != null && fsh1 != null) {
                progRect = buildProgram(vsh1, fsh1);
                System.out.println("[DrawHelper] rounded_rect OK, id=" + progRect);
            } else {
                System.err.println("[DrawHelper] rounded_rect shaders not found!");
            }

            // Шейдер 2
            String vsh2 = readResource(rm, "nocturn-client", "shaders/core/module_card.vsh");
            String fsh2 = readResource(rm, "nocturn-client", "shaders/core/module_card.fsh");
            if (vsh2 != null && fsh2 != null) {
                progCard = buildProgram(vsh2, fsh2);
                System.out.println("[DrawHelper] module_card OK, id=" + progCard);
            } else {
                System.err.println("[DrawHelper] module_card shaders not found!");
            }

            // Один VAO/VBO для обоих шейдеров (одинаковый формат вершин)
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

    private static String readResource(ResourceManager rm, String namespace, String path) {
        try {
            Identifier id  = Identifier.of(namespace, path);
            var        opt = rm.getResource(id);
            if (opt.isEmpty()) { System.err.println("[DrawHelper] Not found: " + id); return null; }
            try (InputStream is = opt.get().getInputStream()) { return new String(is.readAllBytes()); }
        } catch (Exception e) {
            System.err.println("[DrawHelper] readResource error: " + e.getMessage());
            return null;
        }
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
    // ПУБЛИЧНЫЕ МЕТОДЫ — ROUNDED RECT
    // ══════════════════════════════════════════════════════════════

    public static void drawRect(Matrix4f m, float x, float y, float w, float h,
                                float radius, Color color) {
        sdf(m, x, y, w, h, radius, color, null, 0, null, 0, -1);
    }

    public static void drawRect(Matrix4f m, float x, float y, float w, float h, Color color) {
        sdf(m, x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, Color color) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, 0, color, null, 0, null, 0, -1);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h,
                                float radius, Color color) {
        sdf(ctx.getMatrices().peek().getPositionMatrix(),
            x, y, w, h, radius, color, null, 0, null, 0, -1);
    }

    public static void drawStyledRect(Matrix4f m, float x, float y, float w, float h,
                                      float tl, float tr, float br, float bl, Color color) {
        if (tl == tr && tr == br && br == bl) { drawRect(m, x, y, w, h, tl, color); return; }
        float topR = Math.max(tl, tr);
        float botR = Math.max(bl, br);
        float half = h / 2f;
        sdf(m, x, y,        w, half + 1, topR, color, null, 0, null, 0, -1);
        sdf(m, x, y + half, w, half + 1, botR, color, null, 0, null, 0, -1);
        if (tl == 0 && topR > 0) drawRectRaw(m, x,            y,            topR, topR, color);
        if (tr == 0 && topR > 0) drawRectRaw(m, x + w - topR, y,            topR, topR, color);
        if (bl == 0 && botR > 0) drawRectRaw(m, x,            y + h - botR, botR, botR, color);
        if (br == 0 && botR > 0) drawRectRaw(m, x + w - botR, y + h - botR, botR, botR, color);
    }

    public static void drawOutline(MatrixStack ms, float x, float y, float w, float h,
                                   float radius, Color color, float thickness) {
        sdf(ms.peek().getPositionMatrix(),
            x - thickness, y - thickness,
            w + thickness * 2, h + thickness * 2,
            radius + thickness,
            new Color(0, 0, 0, 0), color, thickness, null, 0, -1);
    }

    public static void drawGlow(Matrix4f m, float x, float y, float w, float h,
                                float radius, float spread, Color color) {
        sdf(m, x - spread, y - spread, w + spread * 2f, h + spread * 2f,
            radius + spread, new Color(0, 0, 0, 0), null, 0, color, spread, -1);
    }

    public static void drawCircle(Matrix4f m, float cx, float cy, int segments,
                                  float r, Color color) {
        sdf(m, cx - r, cy - r, r * 2, r * 2, r, color, null, 0, null, 0, -1);
    }

    // ══════════════════════════════════════════════════════════════
    // ПУБЛИЧНЫЕ МЕТОДЫ — MODULE CARD SHADER
    // ══════════════════════════════════════════════════════════════

    /**
     * Рисует карточку модуля с радужным переливанием через module_card шейдер.
     *
     * @param idxOffset  смещение оттенка для каждого модуля (0.0 .. 1.0)
     * @param toggle     анимация включения (0.0 .. 1.0)
     * @param hover      анимация наведения (0.0 .. 1.0)
     * @param rainbowRGB текущий rainbow цвет для этого модуля
     */
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

        setMat4p(progCard, "ModelViewMat", RenderSystem.getModelViewMatrix());
        setMat4p(progCard, "ProjMat",      RenderSystem.getProjectionMatrix());

        u4fp(progCard, "u_Rect",       0, 0, w, h);
        u1fp(progCard, "u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u1fp(progCard, "u_Time",       time);
        u1fp(progCard, "u_Toggle",     toggle);
        u1fp(progCard, "u_Hover",      hover);
        u1fp(progCard, "u_IdxOffset",  idxOffset);
        u3fp(progCard, "u_RainbowRGB",
             rainbowRGB.getRed()/255f,
             rainbowRGB.getGreen()/255f,
             rainbowRGB.getBlue()/255f);

        uploadQuad(x, y, w, h);

        GlStateManager._glUseProgram(0);
        GlStateManager._disableBlend();
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
    // CORE SDF (rounded_rect shader)
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

        setMat4p(progRect, "ModelViewMat", RenderSystem.getModelViewMatrix());
        setMat4p(progRect, "ProjMat",      RenderSystem.getProjectionMatrix());

        u2fp(progRect, "u_Resolution", w, h);
        u4fp(progRect, "u_Rect",       0, 0, w, h);
        u1fp(progRect, "u_Radius",     Math.min(radius, Math.min(w, h) / 2f));
        u4fp(progRect, "u_Color",
             fc.getRed()/255f, fc.getGreen()/255f, fc.getBlue()/255f, fc.getAlpha()/255f);
        u4fp(progRect, "u_OutlineColor",
             oc.getRed()/255f, oc.getGreen()/255f, oc.getBlue()/255f, oc.getAlpha()/255f);
        u1fp(progRect, "u_OutlineWidth", outlineColor != null ? outlineWidth : 0f);
        u4fp(progRect, "u_GlowColor",
             gc.getRed()/255f, gc.getGreen()/255f, gc.getBlue()/255f, gc.getAlpha()/255f);
        u1fp(progRect, "u_GlowRadius", glowColor != null ? glowRadius : 0f);
        u1fp(progRect, "u_Time",       time < 0 ? 0f : time);

        uploadQuad(x, y, w, h);

        GlStateManager._glUseProgram(0);
        GlStateManager._disableBlend();
    }

    // ══════════════════════════════════════════════════════════════
    // VAO/VBO upload
    // ══════════════════════════════════════════════════════════════

    private static void uploadQuad(float x, float y, float w, float h) {
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
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    // ══════════════════════════════════════════════════════════════
    // GL HELPERS — принимают явный programId
    // ══════════════════════════════════════════════════════════════

    private static void u1fp(int prog, String n, float a) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform1f(l, a);
    }
    private static void u2fp(int prog, String n, float a, float b) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform2f(l, a, b);
    }
    private static void u3fp(int prog, String n, float a, float b, float c) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform3f(l, a, b, c);
    }
    private static void u4fp(int prog, String n, float a, float b, float c, float d) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) GL20.glUniform4f(l, a, b, c, d);
    }
    private static void setMat4p(int prog, String n, Matrix4f mat) {
        int l = GL20.glGetUniformLocation(prog, n);
        if (l >= 0) {
            MAT_BUF.clear();
            mat.get(MAT_BUF);
            GL20.glUniformMatrix4fv(l, false, MAT_BUF);
        }
    }
}
