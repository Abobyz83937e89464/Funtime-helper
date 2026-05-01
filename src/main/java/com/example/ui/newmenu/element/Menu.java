package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Menu extends Screen {

    // Размер меню
    private static final int W  = 480;
    private static final int H  = 290;
    private static final int SW = 80;  // ширина sidebar
    private static final float PAD = 6f;
    private static final int COLS = 2;

    private final CategoryElement catEl = new CategoryElement();
    private final ModuleElement   modEl = new ModuleElement();
    private final List<ModuleEntry> modules = new ArrayList<>();

    private float openAnim = 0f;
    private float scrollOffset = 0f, scrollTarget = 0f, maxScroll = 0f;

    public Menu() {
        super(Text.of("Nocturn"));
        loadModules();
    }

    private void loadModules() {
        modules.clear();
        switch (catEl.getSelected()) {
            case COMBAT   -> add("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> add("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> add("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> add("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> add("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> add("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollOffset = 0; scrollTarget = 0;
        recalcScroll();
    }

    private void add(String... names) {
        for (String n : names) modules.add(new ModuleEntry(n, false));
    }

    private void recalcScroll() {
        int rows = (int)Math.ceil((double)modules.size() / COLS);
        float contentH = rows * (ModuleElement.CARD_H + PAD) + PAD;
        float visibleH = H - 26; // минус header
        maxScroll = Math.max(0, contentH - visibleH);
    }

    private float x() { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - W) / 2f; }
    private float y() { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - H) / 2f; }
    private float cardW() { return (W - SW - PAD*(COLS+1)) / COLS; }

    @Override
    public void tick() {
        openAnim     += (1f - openAnim) * 0.14f;
        scrollOffset += (scrollTarget - scrollOffset) * 0.2f;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float x = x(), y = y();
        int a = (int)(255 * openAnim);

        // Тёмный оверлей
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0, 0, 0, (int)(100*openAnim)));

        // Анимация масштаба
        float scale = 0.9f + openAnim*0.1f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x+W/2f, y+H/2f, 0);
        ctx.getMatrices().scale(scale, scale, 1);
        ctx.getMatrices().translate(-(x+W/2f), -(y+H/2f), 0);

        // === Основной фон ===
        DrawHelper.drawRoundedRect(ctx, x, y, W, H, 8, new Color(18, 19, 28, a));
        DrawHelper.drawOutline(ctx, x, y, W, H, 8, new Color(45, 47, 68, a));

        // === Header (26px) ===
        DrawHelper.drawRoundedRect(ctx, x, y, W, 26, 8, new Color(22, 23, 35, a));
        DrawHelper.drawRect(ctx, x, y+20, W, 6, new Color(22, 23, 35, a)); // скругление только сверху
        DrawHelper.drawRect(ctx, x, y+25, W, 1, new Color(38, 40, 60, a));
        DrawHelper.drawGradientH(ctx, x, y, W*0.25f, 26,
            new Color(50, 55, 100, (int)(50*openAnim)), new Color(18,19,28,0));

        // Логотип
        DrawHelper.drawTextShadow(ctx, "Nocturn", x+10, y+8, new Color(190, 195, 255, a));
        int nw = DrawHelper.textWidth("Nocturn");
        DrawHelper.drawText(ctx, " Client", x+10+nw, y+8, new Color(110, 120, 255, a));

        // Правая часть header
        String ver = "1.21.4  |  Fabric";
        DrawHelper.drawText(ctx, ver, x+W-DrawHelper.textWidth(ver)-10, y+8, new Color(65, 68, 100, a));

        // === Sidebar ===
        DrawHelper.drawRect(ctx, x, y+26, SW, H-26, new Color(14, 15, 23, a));
        DrawHelper.drawRect(ctx, x+SW, y+26, 1, H-26, new Color(38, 40, 60, a));
        catEl.render(ctx, x, y+26, SW, H-26);

        // === Content area ===
        float cx = x+SW+1, cy = y+26;
        float cw = W-SW-1, ch = H-26;

        ctx.enableScissor((int)cx, (int)cy, (int)(cx+cw), (int)(cy+ch));

        float cardW = cardW();
        float startY = cy + PAD + scrollOffset;
        for (int i = 0; i < modules.size(); i++) {
            int col = i % COLS, row = i / COLS;
            float mx2 = cx + PAD + col*(cardW+PAD);
            float my2 = startY + row*(ModuleElement.CARD_H+PAD);
            if (my2 + ModuleElement.CARD_H < cy || my2 > cy+ch) continue;
            modEl.render(ctx, mx2, my2, cardW, modules.get(i), mouseX, mouseY);
        }

        ctx.disableScissor();

        // Fade сверху/снизу
        DrawHelper.drawGradientV(ctx, cx, cy, cw, 12,
            new Color(18,19,28,a), new Color(18,19,28,0));
        DrawHelper.drawGradientV(ctx, cx, cy+ch-12, cw, 12,
            new Color(18,19,28,0), new Color(18,19,28,a));

        // Scrollbar
        if (maxScroll > 0) {
            float trackH = ch-8;
            float thumbH = Math.max(20, trackH*ch/(ch+maxScroll));
            float prog = (maxScroll > 0) ? scrollOffset / -maxScroll : 0;
            float thumbY = cy+4 + prog*(trackH-thumbH);
            DrawHelper.drawRoundedRect(ctx, cx+cw-4, cy+4, 3, trackH, 2, new Color(28,30,45,a));
            DrawHelper.drawRoundedRect(ctx, cx+cw-4, thumbY, 3, thumbH, 2, new Color(100,120,255,a));
        }

        ctx.getMatrices().pop();

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x = x(), y = y();

        // Sidebar клик
        if (mouseX >= x && mouseX <= x+SW && mouseY >= y+26 && mouseY <= y+H) {
            if (catEl.click(mouseX, mouseY)) { loadModules(); return true; }
        }

        // Модули клик
        float cx = x+SW+1, cy = y+26;
        float cardW = cardW();
        float startY = cy + PAD + scrollOffset;
        for (int i = 0; i < modules.size(); i++) {
            int col = i%COLS, row = i/COLS;
            float mx2 = cx+PAD+col*(cardW+PAD);
            float my2 = startY+row*(ModuleElement.CARD_H+PAD);
            if (mouseX>=mx2&&mouseX<=mx2+cardW&&mouseY>=my2&&mouseY<=my2+ModuleElement.CARD_H) {
                if (button==0) { modules.get(i).enabled = !modules.get(i).enabled; return true; }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x = x(), y = y();
        if (mouseX>=x+SW&&mouseX<=x+W&&mouseY>=y+26&&mouseY<=y+H) {
            scrollTarget = Math.max(-maxScroll, Math.min(0, scrollTarget+(float)(v*18)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, h, v);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key==256) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override public boolean shouldPause() { return false; }

    public static class ModuleEntry {
        public String name;
        public boolean enabled;
        public ModuleEntry(String n, boolean e) { name=n; enabled=e; }
    }
}
