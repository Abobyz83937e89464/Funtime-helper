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

    private static final int CONTENT_W = 420;
    private static final int CONTENT_H = 190;
    private static final int HEADER_H  = 28;
    private static final int FOOTER_H  = 32;
    private static final int TOTAL_H   = HEADER_H + CONTENT_H + FOOTER_H;

    private static final int   COLS    = 4;
    private static final int   PADDING = 6;

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

    // ── ВАЖНО: отключаем встроенный blur Minecraft 1.21 ──────
    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // ничего не делаем — убирает размытие фона
    }

    // ── Модули по категориям ───────────────────────────────────
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
        scrollValue  = 0;
        scrollTarget = 0;
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

    private float screenX() {
        return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - CONTENT_W) / 2f;
    }
    private float screenY() {
        return (MinecraftClient.getInstance().getWindow().getScaledHeight() - TOTAL_H)   / 2f;
    }

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
        int   a  = Math.min(255, (int)(255 * openAnim));

        // Лёгкий тёмный оверлей (не blur!)
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0, 0, 0, (int)(90 * openAnim)));

        // Анимация открытия
        float sc = 0.95f + openAnim * 0.05f;
        ms.push();
        ms.translate(x + CONTENT_W / 2f, y + TOTAL_H / 2f, 0);
        ms.scale(sc, sc, 1f);
        ms.translate(-(x + CONTENT_W / 2f), -(y + TOTAL_H / 2f), 0);

        // ── HEADER ──────────────────────────────────────────
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W, HEADER_H,
            10, 10, 0, 0,
            new Color(22, 24, 35, a));

        // Градиент-акцент слева в хедере
        DrawHelper.drawGradientH(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W * 0.35f, HEADER_H,
            new Color(51, 56, 94, (int)(45 * openAnim)),
            new Color(22, 24, 35, 0));

        // Логотип
        String logo = "Funtime";
        String sub  = " Helper";
        float logoY = y + (HEADER_H - Fonts.height()) / 2f;
        DrawHelper.drawTextShadow(ctx, logo, x + 10, logoY, new Color(197, 200, 255, a));
        DrawHelper.drawText(ctx, sub, x + 10 + Fonts.boldWidth(logo), logoY, new Color(125, 136, 255, a));

        // Версия справа
        String ver = "v1.0";
        DrawHelper.drawTextMedium(ctx, ver,
            x + CONTENT_W - Fonts.mediumWidth(ver) - 10, logoY,
            new Color(70, 75, 110, a));

        // ── CONTENT ─────────────────────────────────────────
        DrawHelper.drawRect(ms.peek().getPositionMatrix(),
            x, cy, CONTENT_W, CONTENT_H, 0,
            new Color(14, 15, 20, a));

        // Общий outline всего меню
        DrawHelper.drawOutline(ms, x - 1, y - 1, CONTENT_W + 2, TOTAL_H + 2,
            10, new Color(45, 44, 58, a), 2);

        // Тонкая линия под хедером
        DrawHelper.drawRect(ms.peek().getPositionMatrix(),
            x, cy - 1, CONTENT_W, 1, 0,
            new Color(40, 42, 60, a));

        // Scissor для контента
        ctx.enableScissor((int) x, (int) cy, (int)(x + CONTENT_W), (int)(cy + CONTENT_H));
        renderModules(ctx, (int) x + PADDING, (int) cy + PADDING, mouseX, mouseY);
        ctx.disableScissor();

        // Fade сверху/снизу
        DrawHelper.drawGradientV(ctx, x, cy, CONTENT_W, 10,
            new Color(14, 15, 20, a), new Color(14, 15, 20, 0));
        DrawHelper.drawGradientV(ctx, x, cy + CONTENT_H - 10, CONTENT_W, 10,
            new Color(14, 15, 20, 0), new Color(14, 15, 20, a));

        // Scrollbar
        if (maxScroll > 0) {
            float trkH = CONTENT_H - 6;
            float tmbH = Math.max(14, trkH * CONTENT_H / (CONTENT_H + maxScroll));
            float prog  = maxScroll > 0 ? scrollValue / maxScroll : 0f;
            float tmbY  = cy + 3 + prog * (trkH - tmbH);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + CONTENT_W - 3, cy + 3, 2, trkH, 1, new Color(25, 27, 38, a));
            DrawHelper.drawRect(ms.peek().getPositionMatrix(),
                x + CONTENT_W - 3, tmbY,   2, tmbH, 1, new Color(100, 110, 220, a));
        }

        // ── FOOTER ──────────────────────────────────────────
        float fy = cy + CONTENT_H;
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, fy, CONTENT_W, FOOTER_H,
            0, 0, 10, 10,
            new Color(22, 24, 35, a));

        // Разделитель
        DrawHelper.drawRect(ms.peek().getPositionMatrix(),
            x + 8, fy, CONTENT_W - 16, 1, 0,
            new Color(40, 42, 60, a));

        // Категории
        categoryElement.renderFooter(ctx, x, fy, CONTENT_W, FOOTER_H);

        ms.pop();

        // НЕ вызываем super.render() — он тоже может добавить фоновые эффекты
        // Но нам нужны базовые вещи, вызываем вручную только то что нужно
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

            if (my2 + mh >= cy && my2 <= cy + CONTENT_H) {
                moduleElement.render(ctx, colX[col], my2, modules.get(i), mouseX, mouseY);
            }
            colY[col] += (int)(mh + PADDING);
        }
    }

    // ── Events ────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x  = screenX(), y = screenY();
        float fy = y + HEADER_H + CONTENT_H;

        if (mouseY >= fy && mouseY <= fy + FOOTER_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                updateCurrentModules();
                return true;
            }
        }

        float modW = ModuleElement.getModuleWidth();
        int[] colX = new int[COLS];
        for (int i = 0; i < COLS; i++) colX[i] = (int)(x + PADDING) + i * (int)(modW + PADDING);
        int[] colY = new int[COLS];
        Arrays.fill(colY, (int)(y + HEADER_H + PADDING - scrollValue));

        for (int i = 0; i < modules.size(); i++) {
            int col = i % COLS;
            if (moduleElement.mouseClicked(colX[col], colY[col], modules.get(i), mouseX, mouseY, button))
                return true;
            colY[col] += (int)(moduleElement.getHeight() + PADDING);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x = screenX(), y = screenY();
        if (mouseX >= x && mouseX <= x + CONTENT_W &&
            mouseY >= y + HEADER_H && mouseY <= y + HEADER_H + CONTENT_H) {
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget - (float)(v * 14)));
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
        public ModuleEntry(String n, boolean e) { name = n; enabled = e; }
    }
}
