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

    private static final int   W    = 460;
    private static final int   H    = 280;
    private static final int   SW   = 78;
    private static final int   HDR  = 26;
    private static final int   COLS = 2;
    private static final float PAD  = 5f;

    private final CategoryElement catEl = new CategoryElement();
    private final ModuleElement   modEl = new ModuleElement();
    private final List<ModuleEntry> mods = new ArrayList<>();

    private float openAnim  = 0f;
    private float scrollOff = 0f, scrollTgt = 0f, maxScroll = 0f;

    public Menu() {
        super(Text.of("Nocturn"));
        load();
    }

    // ── Загрузка модулей по категории ─────────────────────────

    private void load() {
        mods.clear();
        switch (catEl.getSelected()) {
            case COMBAT   -> add("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> add("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> add("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> add("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> add("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> add("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollOff = 0; scrollTgt = 0;
        recalcScroll();
    }

    private void add(String... names) {
        for (String n : names) mods.add(new ModuleEntry(n, false));
    }

    private void recalcScroll() {
        int rows = (int)Math.ceil((double)mods.size() / COLS);
        float visH = H - HDR;
        maxScroll = Math.max(0, rows*(ModuleElement.H+PAD)+PAD - visH);
    }

    // ── Координаты ────────────────────────────────────────────

    private float x()  { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - W) / 2f; }
    private float y()  { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - H) / 2f; }
    private float cw() { return (W - SW - PAD*(COLS+1)) / COLS; }

    // ── Tick ──────────────────────────────────────────────────

    @Override
    public void tick() {
        openAnim  += (1f - openAnim)          * 0.14f;
        scrollOff += (scrollTgt - scrollOff)  * 0.2f;
    }

    // ── Render ────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float x = x(), y = y();
        int   a = (int)(255 * openAnim);

        // Тёмный оверлей фона
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0, 0, 0, (int)(90*openAnim)));

        // Анимация масштаба при открытии
        float sc = 0.92f + openAnim*0.08f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x + W/2f, y + H/2f, 0);
        ctx.getMatrices().scale(sc, sc, 1f);
        ctx.getMatrices().translate(-(x + W/2f), -(y + H/2f), 0);

        // ── Тень меню ───────────────────────────────────────
        for (int s = 8; s > 0; s--) {
            int sa = (int)(18.0f * ((8-s+1) / 8.0f));
            DrawHelper.drawOutline(ctx, x-s, y-s, W+s*2, H+s*2, 8+s,
                new Color(0, 0, 0, Math.max(sa, 0)));
        }

        // ── Основной фон ────────────────────────────────────
        DrawHelper.drawRoundedRect(ctx, x, y, W, H, 8, new Color(16, 17, 26, a));
        DrawHelper.drawOutline(ctx, x, y, W, H, 8, new Color(40, 42, 63, a));

        // ── Header ──────────────────────────────────────────
        DrawHelper.drawRoundedRect(ctx, x, y, W, HDR, 8, new Color(20, 21, 32, a));
        // Заполняем нижние скруглённые углы header
        DrawHelper.drawRect(ctx, x, y+HDR-8, W, 8, new Color(20, 21, 32, a));
        // Нижняя линия header
        DrawHelper.drawRect(ctx, x, y+HDR-1, W, 1, new Color(35, 37, 57, a));
        // Горизонтальный gradient акцент в header слева
        DrawHelper.drawGradientH(ctx, x, y, W*0.25f, HDR,
            new Color(48, 53, 105, (int)(48*openAnim)),
            new Color(16, 17, 26, 0));
        // Горизонтальный gradient справа
        DrawHelper.drawGradientH(ctx, x+W*0.75f, y, W*0.25f, HDR,
            new Color(16, 17, 26, 0),
            new Color(38, 42, 90, (int)(30*openAnim)));

        // Лого — огромный
        float logoY = y + (HDR - DrawHelper.thHuge()) / 2f;
        DrawHelper.textHuge(ctx, "Nocturn", x+10, logoY, new Color(188, 193, 255, a));
        // " Client" рядом средним
        DrawHelper.textMed(ctx, " Client",
            x + 10 + DrawHelper.twHuge("Nocturn"),
            y + (HDR - DrawHelper.thMed())/2f,
            new Color(100, 112, 255, a));

        // Точка-разделитель в центре header
        float dotX = x + W/2f;
        DrawHelper.drawRoundedRect(ctx, dotX-1, y+HDR-4, 2, 3, 1,
            new Color(120, 136, 255, (int)(160*openAnim)));

        // Категория по центру header
        String catName = catEl.getSelected().getName();
        float catX = x + (W - DrawHelper.twMed(catName))/2f;
        float catY = y + (HDR - DrawHelper.thMed())/2f;
        DrawHelper.textMed(ctx, catName, catX, catY, new Color(138, 142, 200, a));

        // Версия справа
        String ver = "v1.0 | 1.21.4";
        DrawHelper.text(ctx, ver,
            x + W - DrawHelper.tw(ver) - 9,
            y + (HDR - DrawHelper.th())/2f,
            new Color(55, 58, 92, a));

        // ── Sidebar ─────────────────────────────────────────
        DrawHelper.drawRect(ctx, x, y+HDR, SW, H-HDR, new Color(12, 13, 20, a));
        DrawHelper.drawRect(ctx, x+SW, y+HDR, 1, H-HDR, new Color(34, 36, 56, a));
        // Вертикальный gradient в sidebar
        DrawHelper.drawGradientV(ctx, x, y+HDR, SW, (H-HDR)*0.4f,
            new Color(15, 16, 25, a), new Color(12, 13, 20, a));
        catEl.render(ctx, x, y+HDR, SW, H-HDR);

        // ── Content area ────────────────────────────────────
        float cx = x+SW+1, cy = y+HDR, cw = W-SW-1, ch = H-HDR;

        ctx.enableScissor((int)cx, (int)cy, (int)(cx+cw), (int)(cy+ch));

        float cardW  = cw();
        float startY = cy + PAD + scrollOff;

        for (int i = 0; i < mods.size(); i++) {
            int col = i % COLS, row = i / COLS;
            float mx2 = cx + PAD + col*(cardW+PAD);
            float my2 = startY + row*(ModuleElement.H+PAD);
            if (my2 + ModuleElement.H < cy || my2 > cy+ch) continue;
            modEl.render(ctx, mx2, my2, cardW, mods.get(i), mouseX, mouseY);
        }

        ctx.disableScissor();

        // Fade сверху
        DrawHelper.drawGradientV(ctx, cx, cy, cw, 12,
            new Color(16,17,26,a), new Color(16,17,26,0));
        // Fade снизу
        DrawHelper.drawGradientV(ctx, cx, cy+ch-12, cw, 12,
            new Color(16,17,26,0), new Color(16,17,26,a));

        // ── Scrollbar ───────────────────────────────────────
        if (maxScroll > 0) {
            float trkH = ch - 8f;
            float tmbH = Math.max(18f, trkH * ch / (ch + maxScroll));
            float prog  = maxScroll > 0 ? scrollOff / -maxScroll : 0f;
            float tmbY  = cy + 4f + prog*(trkH - tmbH);

            DrawHelper.drawRoundedRect(ctx, cx+cw-4, cy+4, 3, trkH, 2,
                new Color(24, 26, 40, a));
            DrawHelper.drawRoundedRect(ctx, cx+cw-4, tmbY, 3, tmbH, 2,
                new Color(90, 110, 255, a));
        }

        // ── Финальный внешний outline всего меню ────────────
        DrawHelper.drawOutline(ctx, x-1, y-1, W+2, H+2, 9,
            new Color(28, 30, 48, a));

        ctx.getMatrices().pop();
        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Mouse & Keys ──────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x = x(), y = y();

        // Клик по sidebar
        if (mouseX >= x && mouseX <= x+SW && mouseY >= y+HDR && mouseY <= y+H) {
            if (catEl.click(mouseX, mouseY)) { load(); return true; }
        }

        // Клик по модулям
        float cx = x+SW+1, cy = y+HDR, cardW = cw();
        float startY = cy + PAD + scrollOff;
        for (int i = 0; i < mods.size(); i++) {
            int col = i%COLS, row = i/COLS;
            float mx2 = cx+PAD+col*(cardW+PAD);
            float my2 = startY+row*(ModuleElement.H+PAD);
            if (mouseX>=mx2 && mouseX<=mx2+cardW && mouseY>=my2 && mouseY<=my2+ModuleElement.H) {
                if (button == 0) { mods.get(i).enabled = !mods.get(i).enabled; return true; }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x = x(), y = y();
        if (mouseX >= x+SW && mouseX <= x+W && mouseY >= y+HDR && mouseY <= y+H) {
            scrollTgt = Math.max(-maxScroll, Math.min(0, scrollTgt + (float)(v*16)));
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

    // ── ModuleEntry ───────────────────────────────────────────

    public static class ModuleEntry {
        public String  name;
        public boolean enabled;
        public ModuleEntry(String n, boolean e) { name = n; enabled = e; }
    }
}
