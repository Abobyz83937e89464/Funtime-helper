package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu extends Screen {

    // ── Палитра ───────────────────────────────────────────────
    private static final Color BG         = new Color(0x0F, 0x11, 0x17);
    private static final Color HEADER_BG  = new Color(0x13, 0x15, 0x1E);
    private static final Color FOOTER_BG  = new Color(0x13, 0x15, 0x1E);
    private static final Color ACCENT     = new Color(0x6C, 0x7B, 0xFF);
    private static final Color TEXT_MAIN  = new Color(0xE6, 0xE9, 0xFF);
    private static final Color TEXT_DIM   = new Color(0x8A, 0x90, 0xC2);
    private static final Color TEXT_FAINT = new Color(0x45, 0x48, 0x70);
    private static final Color DIVIDER    = new Color(0x22, 0x24, 0x35);
    private static final Color OUTLINE    = new Color(0x2A, 0x2C, 0x3E);

    // ── Размеры ───────────────────────────────────────────────
    private static final int CONTENT_W = 430;
    private static final int CONTENT_H = 200;
    private static final int HEADER_H  = 30;
    private static final int FOOTER_H  = 34;
    private static final int TOTAL_H   = HEADER_H + CONTENT_H + FOOTER_H;
    private static final int COLS      = 4;
    private static final int PADDING   = 8;

    private final CategoryElement        categoryElement = new CategoryElement();
    private final ModuleElement          moduleElement   = new ModuleElement();
    private final List<Menu.ModuleEntry> modules         = new ArrayList<>();

    private float scrollValue  = 0f;
    private float scrollTarget = 0f;
    private float maxScroll    = 0f;
    private float openAnim     = 0f;

    public Menu() {
        super(Text.of("Menu"));
        updateCurrentModules();
    }

    // ── КРИТИЧНО: отключаем blur Minecraft 1.21 ───────────────
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) { }

    // ── Модули ────────────────────────────────────────────────
    private void updateCurrentModules() {
        modules.clear();
        switch (categoryElement.getSelectedCategory()) {
            case COMBAT   -> addMods("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> addMods("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> addMods("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> addMods("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> addMods("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> addMods("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollValue = 0; scrollTarget = 0;
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
        maxScroll = Math.max(0, maxColH - CONTENT_H);
    }

    private float screenX() { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - CONTENT_W) / 2f; }
    private float screenY() { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - TOTAL_H)   / 2f; }

    @Override
    public void tick() {
        super.tick();
        openAnim    += (1f - openAnim)              * 0.18f;
        scrollValue += (scrollTarget - scrollValue) * 0.2f;
        updateMaxScroll();
    }

    // ── Render ────────────────────────────────────────────────
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MatrixStack ms = ctx.getMatrices();
        float x  = screenX();
        float y  = screenY();
        float cy = y + HEADER_H;
        float fy = cy + CONTENT_H;
        int   a  = Math.min(255, (int)(255 * openAnim));

        // Тёмный оверлей (НЕ blur)
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0, 0, 0, (int)(100 * openAnim)));

        // Анимация открытия
        float sc = 0.95f + openAnim * 0.05f;
        ms.push();
        ms.translate(x + CONTENT_W / 2f, y + TOTAL_H / 2f, 0);
        ms.scale(sc, sc, 1f);
        ms.translate(-(x + CONTENT_W / 2f), -(y + TOTAL_H / 2f), 0);

        // ── Тень всего меню ───────────────────────────────────
        DrawHelper.drawShadow(ms, x, y, CONTENT_W, TOTAL_H, 12f, 20f,
            new Color(0, 0, 0, (int)(180 * openAnim)));

        // ── HEADER ────────────────────────────────────────────
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W, HEADER_H,
            10, 10, 0, 0, withAlpha(HEADER_BG, a));

        // Accent gradient слева
        DrawHelper.drawGradientH(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W * 0.4f, HEADER_H,
            new Color(108, 123, 255, (int)(30 * openAnim)),
            new Color(108, 123, 255, 0));

        // Логотип
        float logoY = y + (HEADER_H - Fonts.height()) / 2f;
        DrawHelper.drawTextShadow(ctx, "Funtime", x + 12, logoY, withAlpha(TEXT_MAIN, a));
        DrawHelper.drawText(ctx, " Helper", x + 12 + Fonts.boldWidth("Funtime"), logoY,
            withAlpha(ACCENT, a));

        // Версия справа
        String ver = "v1.0";
        DrawHelper.drawTextMedium(ctx, ver,
            x + CONTENT_W - Fonts.mediumWidth(ver) - 12, logoY,
            withAlpha(TEXT_FAINT, a));

        // ── CONTENT ───────────────────────────────────────────
        DrawHelper.drawRect(ms.peek().getPositionMatrix(), x, cy, CONTENT_W, CONTENT_H, 0,
            withAlpha(BG, a));

        // Outline всего меню
        DrawHelper.drawOutline(ms, x - 1, y - 1, CONTENT_W + 2, TOTAL_H + 2,
            10, withAlpha(OUTLINE, a), 1);

        // Разделитель хедер/контент
        DrawHelper.drawRect(ms.peek().getPositionMatrix(),
            x, cy, CONTENT_W, 1, 0, withAlpha(DIVIDER, a));

        // Scissor
        ctx.enableScissor((int) x, (int) cy, (int)(x + CONTENT_W), (int)(cy + CONTENT_H));
        renderModules(ctx, (int) x + PADDING, (int) cy + PADDING, mouseX, mouseY);
        ctx.disableScissor();

        // Fade сверху/снизу
        DrawHelper.drawGradientV(ctx, x, cy, CONTENT_W, 12,
            withAlpha(BG, a), new Color(BG.getRed(), BG.getGreen(), BG.getBlue(), 0));
        DrawHelper.drawGradientV(ctx, x, cy + CONTENT_H - 12, CONTENT_W, 12,
            new Color(BG.getRed(), BG.getGreen(), BG.getBlue(), 0), withAlpha(BG, a));

        // Scrollbar
        if (maxScroll > 0) {
            float trkH = CONTENT_H - 8;
            float tmbH = Math.max(14, trkH * CONTENT_H / (CONTENT_H + maxScroll));
            float prog  = scrollValue / maxScroll;
            float tmbY  = cy + 4 + prog * (trkH - tmbH);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + CONTENT_W - 4, cy + 4, 3, trkH, 2,
                withAlpha(DIVIDER, a));
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + CONTENT_W - 4, tmbY,   3, tmbH, 2,
                withAlpha(ACCENT, a));
        }

        // ── FOOTER ────────────────────────────────────────────
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, fy, CONTENT_W, FOOTER_H,
            0, 0, 10, 10, withAlpha(FOOTER_BG, a));

        // Разделитель футер/контент
        DrawHelper.drawRect(ms.peek().getPositionMatrix(),
            x, fy, CONTENT_W, 1, 0, withAlpha(DIVIDER, a));

        categoryElement.renderFooter(ctx, x, fy, CONTENT_W, FOOTER_H);

        ms.pop();
    }

    private void renderModules(DrawContext ctx, int startX, int startY, int mouseX, int mouseY) {
        float modW = ModuleElement.getModuleWidth();
        int[] colX = new int[COLS];
        for (int i = 0; i < COLS; i++) colX[i] = startX + i * (int)(modW + PADDING);
        int[] colY = new int[COLS];
        Arrays.fill(colY, (int)(startY - scrollValue));

        float cy = screenY() + HEADER_H;

        for (int i = 0; i < modules.size(); i++) {
            int   col = i % COLS;
            float my2 = colY[col];
            float mh  = moduleElement.getHeight();
            if (my2 + mh >= cy && my2 <= cy + CONTENT_H)
                moduleElement.render(ctx, colX[col], my2, modules.get(i), mouseX, mouseY);
            colY[col] += (int)(mh + PADDING);
        }
    }

    // ── Events ────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x = screenX(), y = screenY();
        float fy = y + HEADER_H + CONTENT_H;

        if (mouseY >= fy && mouseY <= fy + FOOTER_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                updateCurrentModules(); return true;
            }
        }

        float modW = ModuleElement.getModuleWidth();
        int[] colX = new int[COLS];
        for (int i = 0; i < COLS; i++) colX[i] = (int)(x + PADDING) + i * (int)(modW + PADDING);
        int[] colY = new int[COLS];
        Arrays.fill(colY, (int)(y + HEADER_H + PADDING - scrollValue));

        for (int i = 0; i < modules.size(); i++) {
            int col = i % COLS;
            if (moduleElement.mouseClicked(colX[col], colY[col], modules.get(i), mouseX, mouseY, button)) return true;
            colY[col] += (int)(moduleElement.getHeight() + PADDING);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x = screenX(), y = screenY();
        if (mouseX >= x && mouseX <= x + CONTENT_W && mouseY >= y + HEADER_H && mouseY <= y + HEADER_H + CONTENT_H) {
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget - (float)(v * 15)));
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

    // ── Utils ─────────────────────────────────────────────────
    private static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, a));
    }

    public static class ModuleEntry {
        public String  name;
        public boolean enabled;
        public ModuleEntry(String n, boolean e) { name = n; enabled = e; }
    }
}
