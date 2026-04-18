package com.nocturn.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClickGUI extends Screen {

    // ===================== ЦВЕТА =====================
    private static final int BG_DARK       = 0xFF0A0A12;
    private static final int BG_PANEL      = 0xFF12121C;
    private static final int BG_PANEL_LIGHT= 0xFF1A1A26;
    private static final int BG_SIDEBAR    = 0xFF0D0D16;
    private static final int ACCENT_START  = 0xFF7B2FF7; // фиолетовый
    private static final int ACCENT_END    = 0xFFF107A3; // розовый
    private static final int TEXT_PRIMARY  = 0xFFE8E8F0;
    private static final int TEXT_SECONDARY= 0xFF8080A0;
    private static final int TEXT_DIM      = 0xFF50506A;
    private static final int BORDER        = 0xFF20202E;
    private static final int HOVER_OVERLAY = 0x15FFFFFF;

    // ===================== РАЗМЕРЫ =====================
    private int guiWidth = 560;
    private int guiHeight = 360;
    private int guiX, guiY;
    private final int sidebarWidth = 130;
    private final int headerHeight = 42;
    private final int footerHeight = 28;

    // ===================== СОСТОЯНИЕ =====================
    private final Map<String, List<ModuleEntry>> categories = new LinkedHashMap<>();
    private final List<CategoryEntry> categoryList = new ArrayList<>();
    private String activeCategory;
    private float openAnim = 0f;
    private float selectorY = 0f;
    private float selectorTargetY = 0f;
    private float scroll = 0f;
    private float scrollTarget = 0f;
    private long lastTime;

    private String searchText = "";
    private boolean searchFocused = false;

    public ClickGUI() {
        super(Text.literal("Nocturn"));
        initCategories();
    }

    private void initCategories() {
        addCategory("Combat",   "⚔",
                "KillAura", "AutoTotem", "Criticals", "CrystalAura", "HitBox",
                "TriggerBot", "Velocity", "AutoSwap", "BackTrack", "AntiBot");

        addCategory("Movement", "➤",
                "Flight", "Sprint", "NoFall", "NoSlow", "Jesus",
                "HighJump", "AirStuck", "Sneak", "ElytraBooster", "GuiMove");

        addCategory("Render",   "◇",
                "ESP", "Nametags", "HitColor", "HitEffect", "JumpCircle",
                "Particles", "ShulkerViewer", "TNTTimer", "ItemPhysics", "NoRender");

        addCategory("Player",   "☻",
                "FreeCam", "FastBreak", "NoDelay", "Nuker", "Parkour",
                "Eagle", "ProjectileHelper", "NoPush", "AutoEat", "AntiAFK");

        addCategory("World",    "✦",
                "Scaffold", "Timer", "FastPlace", "AntiInteract", "XRay",
                "BlockESP", "AutoFarm", "ChestStealer");

        addCategory("Misc",     "⚙",
                "ClickGUI", "Notifications", "Optimizer", "NameProtect",
                "ItemScroll", "LeaveTracker", "PotionCombiner", "SPJoiner");

        activeCategory = categoryList.get(0).name;
    }

    private void addCategory(String name, String icon, String... modules) {
        List<ModuleEntry> list = new ArrayList<>();
        for (String m : modules) list.add(new ModuleEntry(m));
        categories.put(name, list);
        categoryList.add(new CategoryEntry(name, icon));
    }

    @Override
    protected void init() {
        guiX = (width - guiWidth) / 2;
        guiY = (height - guiHeight) / 2;
        openAnim = 0f;
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastTime) / 1000f, 0.1f);
        lastTime = now;

        // Анимация открытия
        openAnim = lerp(openAnim, 1f, dt * 8f);
        scroll = lerp(scroll, scrollTarget, dt * 14f);

        // Фон
        int fadeAlpha = (int) (openAnim * 180) & 0xFF;
        ctx.fill(0, 0, width, height, (fadeAlpha << 24));

        // Scale анимация
        ctx.getMatrices().push();
        float scale = 0.92f + 0.08f * openAnim;
        ctx.getMatrices().translate(width / 2f, height / 2f, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-width / 2f, -height / 2f, 0);

        // Тень под GUI
        drawShadow(ctx, guiX - 8, guiY - 8, guiWidth + 16, guiHeight + 16);

        // Основной фон
        drawRoundedRect(ctx, guiX, guiY, guiWidth, guiHeight, BG_DARK);

        // ===== HEADER =====
        drawHeader(ctx, mouseX, mouseY);

        // ===== SIDEBAR =====
        drawSidebar(ctx, mouseX, mouseY, dt);

        // ===== CONTENT =====
        drawContent(ctx, mouseX, mouseY);

        // ===== FOOTER =====
        drawFooter(ctx);

        ctx.getMatrices().pop();
    }

    // ===================== HEADER =====================
    private void drawHeader(DrawContext ctx, int mouseX, int mouseY) {
        int x = guiX, y = guiY;
        // Градиентная полоса под заголовком
        ctx.fill(x, y, x + guiWidth, y + headerHeight, BG_PANEL);
        ctx.fill(x, y + headerHeight - 1, x + guiWidth, y + headerHeight, BORDER);

        // Логотип-градиент
        String logo = "Nocturn";
        int logoX = x + 16;
        int logoY = y + 16;
        drawGradientText(ctx, logo, logoX, logoY, ACCENT_START, ACCENT_END);

        // Версия
        ctx.drawText(textRenderer, "v1.0 • 1.21.4", logoX + textRenderer.getWidth(logo) + 6, logoY + 2, TEXT_DIM, false);

        // Поиск
        int searchW = 160;
        int searchX = x + guiWidth - searchW - 16;
        int searchY = y + 12;
        int searchH = 18;
        boolean searchHover = isHover(mouseX, mouseY, searchX, searchY, searchW, searchH);
        drawRoundedRect(ctx, searchX, searchY, searchW, searchH, searchFocused ? BG_PANEL_LIGHT : BG_DARK);
        if (searchFocused) drawRoundedBorder(ctx, searchX, searchY, searchW, searchH, blend(ACCENT_START, ACCENT_END, 0.5f));
        else drawRoundedBorder(ctx, searchX, searchY, searchW, searchH, BORDER);

        ctx.drawText(textRenderer, "⌕", searchX + 6, searchY + 5, TEXT_SECONDARY, false);
        String display = searchText.isEmpty() && !searchFocused ? "Search modules..." : searchText;
        int color = searchText.isEmpty() && !searchFocused ? TEXT_DIM : TEXT_PRIMARY;
        ctx.enableScissor(searchX + 18, searchY, searchX + searchW - 4, searchY + searchH);
        ctx.drawText(textRenderer, display, searchX + 18, searchY + 5, color, false);
        if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = searchX + 18 + textRenderer.getWidth(searchText);
            ctx.fill(cursorX, searchY + 4, cursorX + 1, searchY + 14, TEXT_PRIMARY);
        }
        ctx.disableScissor();
    }

    // ===================== SIDEBAR =====================
    private void drawSidebar(DrawContext ctx, int mouseX, int mouseY, float dt) {
        int x = guiX;
        int y = guiY + headerHeight;
        int h = guiHeight - headerHeight - footerHeight;
        ctx.fill(x, y, x + sidebarWidth, y + h, BG_SIDEBAR);
        ctx.fill(x + sidebarWidth - 1, y, x + sidebarWidth, y + h, BORDER);

        int itemH = 30;
        int startY = y + 10;

        // Selector (плавный)
        int activeIdx = 0;
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).name.equals(activeCategory)) { activeIdx = i; break; }
        }
        selectorTargetY = startY + activeIdx * itemH;
        selectorY = lerp(selectorY, selectorTargetY, dt * 14f);

        // Градиентная полоса слева у выбранного
        int selH = itemH - 6;
        int selY = (int) selectorY + 3;
        drawVerticalGradient(ctx, x + 4, selY, x + 7, selY + selH, ACCENT_START, ACCENT_END);

        // Подсветка выбранного
        drawRoundedRect(ctx, x + 10, selY, sidebarWidth - 18, selH, BG_PANEL);

        for (int i = 0; i < categoryList.size(); i++) {
            CategoryEntry cat = categoryList.get(i);
            int iy = startY + i * itemH;
            boolean hover = isHover(mouseX, mouseY, x + 10, iy + 3, sidebarWidth - 18, itemH - 6);
            boolean active = cat.name.equals(activeCategory);

            if (hover && !active) {
                drawRoundedRect(ctx, x + 10, iy + 3, sidebarWidth - 18, itemH - 6, HOVER_OVERLAY);
            }

            int iconColor = active ? blend(ACCENT_START, ACCENT_END, 0.5f) : TEXT_SECONDARY;
            int textColor = active ? TEXT_PRIMARY : TEXT_SECONDARY;

            ctx.drawText(textRenderer, cat.icon, x + 22, iy + 11, iconColor, false);
            ctx.drawText(textRenderer, cat.name, x + 40, iy + 11, textColor, false);

            // Счётчик активных
            int onCount = 0;
            for (ModuleEntry m : categories.get(cat.name)) if (m.enabled) onCount++;
            if (onCount > 0) {
                String num = String.valueOf(onCount);
                int numW = textRenderer.getWidth(num) + 8;
                int numX = x + sidebarWidth - numW - 14;
                drawRoundedRect(ctx, numX, iy + 8, numW, 14, blend(ACCENT_START, ACCENT_END, 0.5f) & 0x55FFFFFF | 0x55000000);
                ctx.drawText(textRenderer, num, numX + 4, iy + 11, TEXT_PRIMARY, false);
            }
        }
    }

    // ===================== CONTENT =====================
    private void drawContent(DrawContext ctx, int mouseX, int mouseY) {
        int x = guiX + sidebarWidth;
        int y = guiY + headerHeight;
        int w = guiWidth - sidebarWidth;
        int h = guiHeight - headerHeight - footerHeight;

        // Заголовок категории
        ctx.drawText(textRenderer, activeCategory, x + 20, y + 14, TEXT_PRIMARY, false);
        List<ModuleEntry> mods = categories.get(activeCategory);
        int total = mods.size();
        long activeCount = mods.stream().filter(m -> m.enabled).count();
        ctx.drawText(textRenderer, activeCount + " / " + total + " enabled",
                x + 20 + textRenderer.getWidth(activeCategory) + 10, y + 15, TEXT_DIM, false);

        // Линия
        ctx.fill(x + 20, y + 32, x + w - 20, y + 33, BORDER);

        // Список модулей (сетка 2 колонки)
        int listX = x + 16;
        int listY = y + 42;
        int listW = w - 32;
        int listH = h - 50;

        ctx.enableScissor(listX, listY, listX + listW, listY + listH);

        int cols = 2;
        int gap = 8;
        int cardW = (listW - gap * (cols - 1)) / cols;
        int cardH = 44;

        List<ModuleEntry> filtered = new ArrayList<>();
        for (ModuleEntry m : mods) {
            if (searchText.isEmpty() || m.name.toLowerCase().contains(searchText.toLowerCase())) {
                filtered.add(m);
            }
        }

        for (int i = 0; i < filtered.size(); i++) {
            ModuleEntry mod = filtered.get(i);
            int col = i % cols;
            int row = i / cols;
            int cx = listX + col * (cardW + gap);
            int cy = listY + row * (cardH + gap) - (int) scroll;

            if (cy + cardH < listY || cy > listY + listH) continue;

            boolean hover = isHover(mouseX, mouseY, cx, cy, cardW, cardH) && mouseY >= listY && mouseY <= listY + listH;
            mod.hoverAnim = lerp(mod.hoverAnim, hover ? 1f : 0f, 0.2f);
            mod.enableAnim = lerp(mod.enableAnim, mod.enabled ? 1f : 0f, 0.2f);

            // Карточка
            int cardBg = mod.enabled ? BG_PANEL_LIGHT : BG_PANEL;
            drawRoundedRect(ctx, cx, cy, cardW, cardH, cardBg);

            // Подсветка hover
            if (mod.hoverAnim > 0.01f) {
                int a = (int) (mod.hoverAnim * 25) << 24;
                drawRoundedRect(ctx, cx, cy, cardW, cardH, a | 0xFFFFFF);
            }

            // Border слева (акцент, если включен)
            if (mod.enableAnim > 0.01f) {
                int borderH = (int) (cardH * mod.enableAnim);
                int borderY = cy + (cardH - borderH) / 2;
                drawVerticalGradient(ctx, cx, borderY, cx + 3, borderY + borderH, ACCENT_START, ACCENT_END);
            }

            drawRoundedBorder(ctx, cx, cy, cardW, cardH, mod.enabled ? blend(ACCENT_START, ACCENT_END, 0.5f) & 0x55FFFFFF | 0x55000000 : BORDER);

            // Имя
            ctx.drawText(textRenderer, mod.name, cx + 12, cy + 10, mod.enabled ? TEXT_PRIMARY : TEXT_SECONDARY, false);
            ctx.drawText(textRenderer, mod.enabled ? "Enabled" : "Disabled", cx + 12, cy + 24, mod.enabled ? blend(ACCENT_START, ACCENT_END, 0.5f) : TEXT_DIM, false);

            // Toggle switch
            int swW = 26, swH = 14;
            int swX = cx + cardW - swW - 10;
            int swY = cy + (cardH - swH) / 2;
            int bg = blend(BG_DARK, blend(ACCENT_START, ACCENT_END, 0.5f), mod.enableAnim);
            drawRoundedRect(ctx, swX, swY, swW, swH, bg);
            int knobX = swX + 2 + (int) ((swW - swH) * mod.enableAnim);
            drawRoundedRect(ctx, knobX, swY + 2, swH - 4, swH - 4, 0xFFFFFFFF);
        }

        ctx.disableScissor();

        // Скроллбар
        int rows = (filtered.size() + cols - 1) / cols;
        int contentH = rows * (cardH + gap);
        if (contentH > listH) {
            int sbX = x + w - 6;
            int sbY = listY;
            int sbH = listH;
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, BG_PANEL);
            int thumbH = Math.max(20, (int) (sbH * ((float) sbH / contentH)));
            int thumbY = sbY + (int) ((sbH - thumbH) * (scroll / Math.max(1, contentH - sbH)));
            drawVerticalGradient(ctx, sbX, thumbY, sbX + 3, thumbY + thumbH, ACCENT_START, ACCENT_END);
        }
    }

    // ===================== FOOTER =====================
    private void drawFooter(DrawContext ctx) {
        int x = guiX;
        int y = guiY + guiHeight - footerHeight;
        ctx.fill(x, y, x + guiWidth, y + footerHeight, BG_PANEL);
        ctx.fill(x, y, x + guiWidth, y + 1, BORDER);

        String server = MinecraftClient.getInstance().getCurrentServerEntry() != null
                ? MinecraftClient.getInstance().getCurrentServerEntry().address
                : "Singleplayer";
        ctx.drawText(textRenderer, "● " + server, x + 16, y + 10, TEXT_SECONDARY, false);

        int total = 0, active = 0;
        for (List<ModuleEntry> list : categories.values()) {
            total += list.size();
            for (ModuleEntry m : list) if (m.enabled) active++;
        }
        String right = active + "/" + total + " active";
        int rw = textRenderer.getWidth(right);
        ctx.drawText(textRenderer, right, x + guiWidth - rw - 16, y + 10, TEXT_SECONDARY, false);
    }

    // ===================== INPUT =====================
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Поиск фокус
        int searchW = 160;
        int searchX = guiX + guiWidth - searchW - 16;
        int searchY = guiY + 12;
        searchFocused = isHover((int) mx, (int) my, searchX, searchY, searchW, 18);

        // Категории
        int itemH = 30;
        int startY = guiY + headerHeight + 10;
        for (int i = 0; i < categoryList.size(); i++) {
            int iy = startY + i * itemH;
            if (isHover((int) mx, (int) my, guiX + 10, iy + 3, sidebarWidth - 18, itemH - 6)) {
                activeCategory = categoryList.get(i).name;
                scroll = 0;
                scrollTarget = 0;
                return true;
            }
        }

        // Модули
        int cx0 = guiX + sidebarWidth + 16;
        int cy0 = guiY + headerHeight + 42;
        int listW = guiWidth - sidebarWidth - 32;
        int listH = guiHeight - headerHeight - footerHeight - 50;
        int cols = 2, gap = 8;
        int cardW = (listW - gap * (cols - 1)) / cols;
        int cardH = 44;

        List<ModuleEntry> mods = categories.get(activeCategory);
        List<ModuleEntry> filtered = new ArrayList<>();
        for (ModuleEntry m : mods) {
            if (searchText.isEmpty() || m.name.toLowerCase().contains(searchText.toLowerCase())) filtered.add(m);
        }

        for (int i = 0; i < filtered.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            int cx = cx0 + col * (cardW + gap);
            int cy = cy0 + row * (cardH + gap) - (int) scroll;
            if (cy + cardH < cy0 || cy > cy0 + listH) continue;
            if (isHover((int) mx, (int) my, cx, cy, cardW, cardH) && my >= cy0 && my <= cy0 + listH) {
                filtered.get(i).enabled = !filtered.get(i).enabled;
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        scrollTarget -= vAmt * 20;
        if (scrollTarget < 0) scrollTarget = 0;
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char ch, int mods) {
        if (searchFocused && ch >= 32 && ch < 127) {
            searchText += ch;
            return true;
        }
        return super.charTyped(ch, mods);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ===================== UTILS =====================
    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private float lerp(float a, float b, float t) {
        t = MathHelper.clamp(t, 0f, 1f);
        return a + (b - a) * t;
    }

    private int blend(int c1, int c2, float t) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        // Имитация закруглённых углов (обрезаем углы 1px)
        ctx.fill(x + 1, y, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 1, y, x + w - 1, y + 1, color);
        ctx.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private void drawVerticalGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int top, int bottom) {
        ctx.fillGradient(x1, y1, x2, y2, top, bottom);
    }

    private void drawShadow(DrawContext ctx, int x, int y, int w, int h) {
        for (int i = 0; i < 6; i++) {
            int a = (int) ((1f - i / 6f) * 40);
            ctx.fill(x - i, y - i, x + w + i, y + h + i, (a << 24));
        }
    }

    private void drawGradientText(DrawContext ctx, String text, int x, int y, int c1, int c2) {
        // Символы с чередованием цвета — имитация градиента
        for (int i = 0; i < text.length(); i++) {
            float t = text.length() == 1 ? 0.5f : (float) i / (text.length() - 1);
            int c = blend(c1, c2, t);
            String ch = String.valueOf(text.charAt(i));
            ctx.drawText(textRenderer, ch, x, y, c, false);
            x += textRenderer.getWidth(ch);
        }
    }

    // ===================== MODELS =====================
    private static class ModuleEntry {
        String name;
        boolean enabled = false;
        float hoverAnim = 0f;
        float enableAnim = 0f;
        ModuleEntry(String n) { this.name = n; }
    }

    private static class CategoryEntry {
        String name;
        String icon;
        CategoryEntry(String n, String i) { this.name = n; this.icon = i; }
    }
    }
