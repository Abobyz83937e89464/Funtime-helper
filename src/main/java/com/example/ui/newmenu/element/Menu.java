package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu extends Screen {

    static final int   CONTENT_W = 370;
    static final int   CONTENT_H = 180;
    static final int   HEADER_H  = 26;
    static final int   FOOTER_H  = 28;
    static final int   TOTAL_H   = HEADER_H + CONTENT_H + FOOTER_H;
    static final int   COLS      = 4;
    static final int   PADDING   = 6;
    static final float MOD_W     = (CONTENT_W - (float) PADDING * (COLS + 1)) / COLS;

    private final CategoryElement   categoryElement = new CategoryElement();
    private final ModuleElement     moduleElement   = new ModuleElement();
    private final List<ModuleEntry> modules         = new ArrayList<>();

    private float scrollValue  = 0f;
    private float scrollTarget = 0f;
    private float maxScroll    = 0f;
    private float openAnim     = 0f;

    public Menu() {
        super(Text.of("Menu"));
        updateCurrentModules();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Убираем стандартный Minecraft blur/overlay
    }

    private void updateCurrentModules() {
        modules.clear();
        switch (categoryElement.getSelectedCategory()) {
            case COMBAT   -> addMods("KillAura","Velocity","AutoTotem","Criticals",
                                     "Reach","AimAssist","AutoPot","AntiBot",
                                     "Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> addMods("Speed","Fly","Sprint","NoSlow",
                                     "Step","Strafe","LongJump","Blink");
            case RENDER   -> addMods("ESP","Tracers","NameTags","Fullbright",
                                     "NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> addMods("AutoArmor","ChestStealer","FastPlace",
                                     "NoFall","Scaffold","AutoEat");
            case WORLD    -> addMods("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> addMods("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollValue  = 0f;
        scrollTarget = 0f;
        updateMaxScroll();
    }

    private void addMods(String... names) {
        for (String n : names) modules.add(new ModuleEntry(n, false));
    }

    private void updateMaxScroll() {
        int[] colH = new int[COLS];
        for (int i = 0; i < modules.size(); i++)
            colH[i % COLS] += (int)(moduleElement.getHeight() + PADDING);
        int maxColH = 0;
        for (int h : colH) maxColH = Math.max(maxColH, h);
        maxScroll = Math.max(0f, maxColH - CONTENT_H + PADDING);
    }

    private float screenX() {
        return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - CONTENT_W) / 2f;
    }
    private float screenY() {
        return (MinecraftClient.getInstance().getWindow().getScaledHeight() - TOTAL_H)   / 2f;
    }

    @Override
    public void tick() {
        super.tick();
        openAnim    += (1f - openAnim)              * 0.20f;
        scrollValue += (scrollTarget - scrollValue) * 0.24f;
        updateMaxScroll();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MatrixStack ms = ctx.getMatrices();
        float x  = screenX();
        float y  = screenY();
        float cy = y + HEADER_H;
        float fy = cy + CONTENT_H;
        int   a  = (int)(255 * openAnim);

        float sc = 0.94f + openAnim * 0.06f;
        ms.push();
        ms.translate(x + CONTENT_W / 2f, y + TOTAL_H / 2f, 0);
        ms.scale(sc, sc, 1f);
        ms.translate(-(x + CONTENT_W / 2f), -(y + TOTAL_H / 2f), 0);

        Matrix4f m = ms.peek().getPositionMatrix();

        // ✅ Фикс серого слоя — уменьшен spread и alpha
        DrawHelper.drawGlow(m, x, y, CONTENT_W, TOTAL_H, 10, 4,
            new Color(80, 90, 200, (int)(18 * openAnim)));

        // ── HEADER ────────────────────────────────────────────────
        DrawHelper.drawStyledRect(m, x, y, CONTENT_W, HEADER_H, 12, 12, 0, 0,
            new Color(22, 24, 34, a));
        DrawHelper.drawGradientH(m, x, y, CONTENT_W * 0.5f, HEADER_H,
            new Color(55, 60, 105, (int)(50 * openAnim)),
            new Color(22, 24, 34, 0));

        // ✅ Сначала рисуем ВСЮ геометрию, потом текст — чтобы шейдер не сбивал шрифт
        float textY = y + (HEADER_H - Fonts.height()) / 2f;

        // Геометрия контента
        DrawHelper.drawRect(m, x, cy, CONTENT_W, CONTENT_H, 0,
            new Color(14, 16, 21, a));
        DrawHelper.drawRectRaw(m, x, cy, CONTENT_W, 1,
            new Color(42, 41, 62, a));

        // Геометрия footer
        DrawHelper.drawRectRaw(m, x, fy, CONTENT_W, 1,
            new Color(42, 41, 62, a));
        DrawHelper.drawStyledRect(m, x, fy, CONTENT_W, FOOTER_H, 0, 0, 12, 12,
            new Color(22, 24, 34, a));
        DrawHelper.drawGradientH(m, x, fy, CONTENT_W * 0.5f, FOOTER_H,
            new Color(45, 50, 95, (int)(40 * openAnim)),
            new Color(22, 24, 34, 0));

        // Outline меню
        DrawHelper.drawOutline(ms, x - 1, y - 1, CONTENT_W + 2, TOTAL_H + 2,
            12, new Color(48, 47, 68, a), 1);

        // Модули (карточки)
        ctx.enableScissor((int)x, (int)cy, (int)(x + CONTENT_W), (int)fy);
        renderModules(ctx, ms, (int)(x + PADDING), (int)(cy + PADDING), mouseX, mouseY);
        ctx.disableScissor();

        // Fade
        DrawHelper.drawGradientV(ctx, x, cy, CONTENT_W, 10,
            new Color(14, 16, 21, a), new Color(14, 16, 21, 0));
        DrawHelper.drawGradientV(ctx, x, fy - 10, CONTENT_W, 10,
            new Color(14, 16, 21, 0), new Color(14, 16, 21, a));

        // Scrollbar
        if (maxScroll > 1f) {
            float trkH = CONTENT_H - 8;
            float tmbH = Math.max(12, trkH * CONTENT_H / (CONTENT_H + maxScroll));
            float prog  = scrollValue / maxScroll;
            float tmbY  = cy + 4 + prog * (trkH - tmbH);
            DrawHelper.drawRect(m, x + CONTENT_W - 4, cy + 4, 3, trkH, 2,
                new Color(26, 28, 40, a));
            DrawHelper.drawRect(m, x + CONTENT_W - 4, tmbY, 3, tmbH, 2,
                new Color(125, 136, 255, a));
        }

        // ✅ Весь текст — В КОНЦЕ, после всей геометрии
        // Header текст
        DrawHelper.drawTextBoldShadow(ctx, "Nocturn", x + 10, textY,
            new Color(197, 200, 255, a));
        DrawHelper.drawTextShadow(ctx, " Client",
            x + 10 + Fonts.boldWidth("Nocturn"), textY,
            new Color(125, 136, 255, a));

        String catName = categoryElement.getSelectedCategory().getDisplayName();
        DrawHelper.drawText(ctx, catName,
            x + (CONTENT_W - Fonts.width(catName)) / 2f, textY,
            new Color(130, 133, 180, a));

        String ver = "v1.0 / 1.21.4";
        DrawHelper.drawText(ctx, ver,
            x + CONTENT_W - Fonts.width(ver) - 10, textY,
            new Color(55, 58, 88, a));

        // Footer
        categoryElement.renderFooter(ctx, x, fy, CONTENT_W, FOOTER_H, openAnim);

        ms.pop();
    }

    private void renderModules(DrawContext ctx, MatrixStack ms,
                               int startX, int startY, int mx, int my) {
        int[] colX = new int[COLS];
        for (int i = 0; i < COLS; i++)
            colX[i] = startX + i * (int)(MOD_W + PADDING);

        int[] colY = new int[COLS];
        Arrays.fill(colY, (int)(startY - scrollValue));

        float cy  = screenY() + HEADER_H;
        float cy2 = cy + CONTENT_H;

        for (int i = 0; i < modules.size(); i++) {
            int   col  = i % COLS;
            float modY = colY[col];
            float modH = moduleElement.getHeight();

            if (modY + modH >= cy - modH && modY <= cy2 + modH)
                moduleElement.render(ctx, ms, colX[col], modY, MOD_W,
                                     modules.get(i), mx, my);

            colY[col] += (int)(modH + PADDING);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x  = screenX();
        float cy = screenY() + HEADER_H;
        float fy = cy + CONTENT_H;

        if (mouseY >= fy && mouseY <= fy + FOOTER_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                updateCurrentModules();
                return true;
            }
        }

        if (mouseY >= cy && mouseY <= fy) {
            int[] colX = new int[COLS];
            for (int i = 0; i < COLS; i++)
                colX[i] = (int)(x + PADDING) + i * (int)(MOD_W + PADDING);
            int[] colY = new int[COLS];
            Arrays.fill(colY, (int)(cy + PADDING - scrollValue));
            for (int i = 0; i < modules.size(); i++) {
                int col = i % COLS;
                if (moduleElement.mouseClicked(colX[col], colY[col], MOD_W,
                        modules.get(i), mouseX, mouseY, button))
                    return true;
                colY[col] += (int)(moduleElement.getHeight() + PADDING);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x  = screenX();
        float cy = screenY() + HEADER_H;
        if (mouseX >= x && mouseX <= x + CONTENT_W &&
            mouseY >= cy && mouseY <= cy + CONTENT_H) {
            scrollTarget = Math.max(0f, Math.min(maxScroll, scrollTarget - (float)(v * 16)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, h, v);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override public boolean shouldPause() { return false; }

    public static class ModuleEntry {
        public String  name;
        public boolean enabled;
        public ModuleEntry(String name, boolean enabled) {
            this.name    = name;
            this.enabled = enabled;
        }
    }
}
