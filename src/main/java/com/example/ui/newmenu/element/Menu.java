package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Menu extends Screen {

    private final CategoryElement categoryElement;
    private final ModuleElement   moduleElement;

    private float scrollOffset = 0f, scrollTarget = 0f, maxScroll = 0f, openAnim = 0f;

    private static final int MENU_W = 860, MENU_H = 440, HEAD_H = 42, FOOT_H = 40;
    private static final int COLS = 4;
    private static final float PAD = 10f;

    private final List<ModuleEntry> modules = new ArrayList<>();

    public Menu() {
        super(Text.of("Nocturn"));
        categoryElement = new CategoryElement();
        moduleElement   = new ModuleElement();
        loadModules();
    }

    private void loadModules() {
        modules.clear();
        switch (categoryElement.getSelectedCategory()) {
            case COMBAT   -> fill("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> fill("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> fill("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> fill("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> fill("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> fill("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollOffset = 0; scrollTarget = 0;
        updateMaxScroll();
    }

    private void fill(String... names) {
        for (String n : names) modules.add(new ModuleEntry(n, false));
    }

    private void updateMaxScroll() {
        int rows = (int) Math.ceil((double) modules.size() / COLS);
        float contentH = rows * (moduleElement.getHeight() + PAD) + PAD;
        maxScroll = Math.max(0, contentH - MENU_H);
    }

    private float menuX() { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - MENU_W) / 2f; }
    private float menuY() { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - MENU_H) / 2f; }
    private float cardW() { return (MENU_W - PAD * (COLS + 1)) / COLS; }

    @Override
    public void tick() {
        super.tick();
        openAnim     += (1f - openAnim)                * 0.12f;
        scrollOffset += (scrollTarget - scrollOffset)  * 0.18f;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float mx = menuX(), my = menuY();
        float totalH = HEAD_H + MENU_H + FOOT_H;

        // Затемнение фона
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0, 0, 0, (int)(130 * openAnim)));

        // Анимация открытия
        float scale = 0.88f + openAnim * 0.12f;
        float cx = mx + MENU_W / 2f, cy = my + MENU_H / 2f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(scale, scale, 1);
        ctx.getMatrices().translate(-cx, -cy, 0);

        // Тень
        DrawHelper.drawShadow(ctx, mx, my - HEAD_H, MENU_W, totalH, 10);

        // Header
        DrawHelper.drawRoundedRect(ctx, mx, my - HEAD_H, MENU_W, HEAD_H, 12,
            new Color(22, 24, 34, (int)(255*openAnim)));
        DrawHelper.drawGradientRectH(ctx, mx, my - HEAD_H, MENU_W * 0.3f, HEAD_H,
            new Color(51, 56, 94, (int)(70*openAnim)), new Color(22, 24, 34, 0));
        renderHeader(ctx, mx, my - HEAD_H);

        // Разделитель header/content
        DrawHelper.drawRect(ctx, mx, my - 1, MENU_W, 1, new Color(40, 42, 60, (int)(255*openAnim)));

        // Main bg
        DrawHelper.drawRect(ctx, mx, my, MENU_W, MENU_H, new Color(17, 19, 24, (int)(255*openAnim)));

        // Content
        renderContent(ctx, mx, my, mouseX, mouseY);

        // Разделитель content/footer
        DrawHelper.drawRect(ctx, mx, my + MENU_H, MENU_W, 1, new Color(40, 42, 60, (int)(255*openAnim)));

        // Footer
        DrawHelper.drawRoundedRect(ctx, mx, my + MENU_H + 1, MENU_W, FOOT_H - 1, 12,
            new Color(22, 24, 34, (int)(255*openAnim)));
        categoryElement.render(ctx, mx, my + MENU_H + 1, MENU_W, FOOT_H - 1);

        // Общий outline
        DrawHelper.drawRoundedOutline(ctx, mx-1, my-HEAD_H-1, MENU_W+2, totalH+2, 12,
            new Color(52, 51, 64, (int)(255*openAnim)), 1);

        ctx.getMatrices().pop();
    }

    private void renderHeader(DrawContext ctx, float x, float y) {
        int a = (int)(255 * openAnim);

        float ty = y + (HEAD_H - Fonts.getBold().fontHeight) / 2f;

        // Логотип слева
        DrawHelper.drawTextShadow(ctx, Fonts.getBold(), "Nocturn",
            x + 16, ty, new Color(197, 200, 255, a));
        DrawHelper.drawText(ctx, Fonts.getRegular(), " Client",
            x + 16 + Fonts.getBold().getWidth("Nocturn"), ty, new Color(125, 136, 255, a));

        // Версия справа
        String ver = "v1.0  |  1.21.4";
        DrawHelper.drawText(ctx, Fonts.getRegular(), ver,
            x + MENU_W - Fonts.getRegular().getWidth(ver) - 16, ty,
            new Color(80, 84, 120, a));

        // Категория по центру
        String cat = categoryElement.getSelectedCategory().getDisplayName();
        DrawHelper.drawText(ctx, Fonts.getRegular(), cat,
            x + (MENU_W - Fonts.getRegular().getWidth(cat)) / 2f, ty,
            new Color(141, 144, 199, a));

        // Точка под категорией
        DrawHelper.drawRoundedRect(ctx, x + MENU_W/2f - 2, y + HEAD_H - 4, 4, 3, 1,
            new Color(125, 136, 255, (int)(180*openAnim)));
    }

    private void renderContent(DrawContext ctx, float x, float y, int mouseX, int mouseY) {
        float cw = cardW(), ch = moduleElement.getHeight();
        ctx.enableScissor((int)x, (int)y, (int)(x+MENU_W), (int)(y+MENU_H));

        float startY = y + PAD + scrollOffset;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % COLS, row = i / COLS;
            float cx2 = x + PAD + col * (cw + PAD);
            float cy2 = startY + row * (ch + PAD);
            if (cy2 + ch < y || cy2 > y + MENU_H) continue;
            moduleElement.render(ctx, cx2, cy2, cw, modules.get(i), mouseX, mouseY);
        }

        ctx.disableScissor();

        // Fade сверху и снизу
        DrawHelper.drawGradientRect(ctx, x, y, MENU_W, 18,
            new Color(17,19,24,(int)(220*openAnim)), new Color(17,19,24,0));
        DrawHelper.drawGradientRect(ctx, x, y+MENU_H-18, MENU_W, 18,
            new Color(17,19,24,0), new Color(17,19,24,(int)(220*openAnim)));

        // Scrollbar
        if (maxScroll > 0) {
            float trackH = MENU_H - 8;
            float thumbH = Math.max(30, trackH * MENU_H / (MENU_H + maxScroll));
            float prog   = scrollOffset / -maxScroll;
            float thumbY = y + 4 + prog * (trackH - thumbH);
            DrawHelper.drawRoundedRect(ctx, x+MENU_W-4, y+4, 3, trackH, 2,
                new Color(28,30,42));
            DrawHelper.drawRoundedRect(ctx, x+MENU_W-4, thumbY, 3, thumbH, 2,
                new Color(125,136,255,200));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x = menuX(), y = menuY();

        // Footer — категории
        if (mouseY >= y+MENU_H+1 && mouseY <= y+MENU_H+FOOT_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) { loadModules(); return true; }
        }

        // Content — модули
        if (mouseY >= y && mouseY <= y+MENU_H) {
            float cw = cardW(), ch = moduleElement.getHeight();
            float startY = y + PAD + scrollOffset;
            for (int i = 0; i < modules.size(); i++) {
                int col = i%COLS, row = i/COLS;
                float cx2 = x+PAD+col*(cw+PAD), cy2 = startY+row*(ch+PAD);
                if (mouseX>=cx2&&mouseX<=cx2+cw&&mouseY>=cy2&&mouseY<=cy2+ch) {
                    if (button==0) { modules.get(i).enabled = !modules.get(i).enabled; return true; }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x = menuX(), y = menuY();
        if (mouseX>=x && mouseX<=x+MENU_W && mouseY>=y && mouseY<=y+MENU_H) {
            scrollTarget = Math.max(-maxScroll, Math.min(0, scrollTarget+(float)(v*22)));
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
        public String name;
        public boolean enabled;
        public ModuleEntry(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }
    }
}
