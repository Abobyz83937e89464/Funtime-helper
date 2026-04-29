package com.example.ui.menu;

import com.example.ui.menu.element.CategoryElement;
import com.example.ui.menu.element.CategoryElement.Category;
import com.example.ui.menu.element.ModuleElement;
import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.*;

public class Menu extends Screen {

    // ─── Компоненты ──────────────────────────────────────────────────────────

    private final CategoryElement categoryElement = new CategoryElement();
    private final ModuleElement   moduleElement   = new ModuleElement();

    // ─── Размеры меню (в пикселях GUI-scale) ─────────────────────────────────

    private static final int MENU_W     = 1100;
    private static final int MENU_H     = 540;
    private static final int HEADER_H   = 80;
    private static final int FOOTER_H   = 80;

    // ─── Колонки ─────────────────────────────────────────────────────────────

    private static final int COLUMNS     = 4;

    // ─── Скролл ──────────────────────────────────────────────────────────────

    private float scrollOffset  = 0f;
    private float maxScroll     = 0f;
    private float scrollVelocity = 0f; // инерция

    // ─── Модули ──────────────────────────────────────────────────────────────

    private final Map<Category, List<ModuleEntry>> modulesByCategory = new LinkedHashMap<>();
    private List<ModuleEntry> currentModules = new ArrayList<>();

    // ─── Конструктор ─────────────────────────────────────────────────────────

    public Menu() {
        super(Text.of("Nocturn Client"));
        initModules();
        updateCurrentModules();
    }

    private void initModules() {
        modulesByCategory.put(Category.COMBAT, Arrays.asList(
                new ModuleEntry("KillAura"),
                new ModuleEntry("Velocity"),
                new ModuleEntry("AutoTotem"),
                new ModuleEntry("Criticals"),
                new ModuleEntry("Reach"),
                new ModuleEntry("AimAssist"),
                new ModuleEntry("AutoPot"),
                new ModuleEntry("AntiBot"),
                new ModuleEntry("WTap"),
                new ModuleEntry("Backtrack")
        ));
        modulesByCategory.put(Category.MOVEMENT, Arrays.asList(
                new ModuleEntry("Speed"),
                new ModuleEntry("Fly"),
                new ModuleEntry("Sprint"),
                new ModuleEntry("NoSlow"),
                new ModuleEntry("Step"),
                new ModuleEntry("Strafe"),
                new ModuleEntry("LongJump"),
                new ModuleEntry("Parkour")
        ));
        modulesByCategory.put(Category.RENDER, Arrays.asList(
                new ModuleEntry("ESP"),
                new ModuleEntry("Tracers"),
                new ModuleEntry("NameTags"),
                new ModuleEntry("Fullbright"),
                new ModuleEntry("NoRender"),
                new ModuleEntry("Chams"),
                new ModuleEntry("HUD")
        ));
        modulesByCategory.put(Category.PLAYER, Arrays.asList(
                new ModuleEntry("AutoArmor"),
                new ModuleEntry("ChestStealer"),
                new ModuleEntry("FastPlace"),
                new ModuleEntry("NoFall"),
                new ModuleEntry("AntiAFK"),
                new ModuleEntry("Scaffold")
        ));
        modulesByCategory.put(Category.WORLD, Arrays.asList(
                new ModuleEntry("Timer"),
                new ModuleEntry("Nuker"),
                new ModuleEntry("Phase"),
                new ModuleEntry("Jesus"),
                new ModuleEntry("AutoMine")
        ));
        modulesByCategory.put(Category.MISC, Arrays.asList(
                new ModuleEntry("AutoFish"),
                new ModuleEntry("Disabler"),
                new ModuleEntry("Spammer"),
                new ModuleEntry("ChatFilter"),
                new ModuleEntry("AntiCheat")
        ));
    }

    private void updateCurrentModules() {
        currentModules = modulesByCategory.getOrDefault(categoryElement.getSelectedCategory(), Collections.emptyList());
        scrollOffset = 0f;
        scrollVelocity = 0f;
        updateMaxScroll();
    }

    // ─── Позиция меню (GUI-координаты) ────────────────────────────────────────

    private int menuX() {
        return (mc.getWindow().getScaledWidth()  - MENU_W) / 2;
    }

    private int menuY() {
        return (mc.getWindow().getScaledHeight() - MENU_H) / 2;
    }

    // ─── Тик ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        // Инерция скролла
        scrollVelocity *= 0.85f;
        scrollOffset += scrollVelocity;
        clampScroll();
        updateMaxScroll();
    }

    private void updateMaxScroll() {
        int cardH = moduleElement.getHeight();
        int rows  = (int) Math.ceil((double) currentModules.size() / COLUMNS);
        int contentH = rows * (cardH + ModuleElement.CARD_PADDING) - ModuleElement.CARD_PADDING;
        int viewportH = MENU_H - 32;
        maxScroll = Math.max(0, contentH - viewportH);
    }

    private void clampScroll() {
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
        if (scrollOffset > 0)          scrollOffset = 0;
    }

    // ─── Render ──────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = menuX();
        int y = menuY();
        TextRenderer tr = mc.textRenderer;

        // === Затемнение фона ===
        DrawHelper.drawRect(context, 0, 0,
                mc.getWindow().getScaledWidth(),
                mc.getWindow().getScaledHeight(),
                new Color(0, 0, 0, 100));

        // === Тень ===
        DrawHelper.drawShadow(context, x - 2, y - HEADER_H - 2,
                MENU_W + 4, HEADER_H + MENU_H + FOOTER_H + 4, 8);

        // === Outline вокруг всего ===
        DrawHelper.drawOutlineRect(context,
                x - 2, y - HEADER_H - 2,
                MENU_W + 4, HEADER_H + MENU_H + FOOTER_H + 4,
                new Color(52, 51, 64), 2);

        // === HEADER ===
        DrawHelper.drawRect(context, x, y - HEADER_H, MENU_W, HEADER_H, new Color(29, 31, 44, 204));
        renderHeader(context, x, y - HEADER_H, MENU_W, HEADER_H, tr);

        // === FOOTER ===
        DrawHelper.drawRect(context, x, y + MENU_H, MENU_W, FOOTER_H, new Color(29, 31, 44, 204));
        renderFooter(context, x, y + MENU_H, MENU_W, FOOTER_H, tr);
        categoryElement.render(context, x, y + MENU_H, MENU_W, FOOTER_H);

        // === MAIN BACKGROUND ===
        DrawHelper.drawRect(context, x, y, MENU_W, MENU_H, new Color(17, 19, 24));

        // Горизонтальные разделители
        DrawHelper.drawRect(context, x, y - 1,      MENU_W, 1, new Color(45, 44, 58));
        DrawHelper.drawRect(context, x, y + MENU_H, MENU_W, 1, new Color(45, 44, 58));

        // === CONTENT ===
        context.enableScissor(x, y, x + MENU_W, y + MENU_H);
        renderModules(context, x + 16, y + 16, mouseX, mouseY);
        context.disableScissor();

        // === Scroll bar ===
        renderScrollBar(context, x + MENU_W - 4, y, 3, MENU_H);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext context, int x, int y, int w, int h, TextRenderer tr) {
        // Клиентское имя
        String title = "Nocturn Client";
        int titleX = x + 16;
        int titleY = y + (h - tr.fontHeight) / 2;

        // Подсветка под заголовком
        int tw = tr.getWidth(title);
        DrawHelper.drawGradientRectH(context, titleX - 2, titleY - 2, tw + 24, tr.fontHeight + 4,
                new Color(51, 56, 94, 100), new Color(51, 56, 94, 0));

        // Акцентная полоска слева
        DrawHelper.drawAccentBar(context, titleX - 6, titleY - 2, 3, tr.fontHeight + 4);

        context.drawText(tr, title, titleX, titleY, new Color(197, 200, 255).getRGB(), false);

        // Версия и счётчик модулей справа
        String info = "v1.0  |  Modules: " + currentModules.size();
        context.drawText(tr, info,
                x + w - tr.getWidth(info) - 16,
                titleY, new Color(80, 84, 110).getRGB(), false);
    }

    private void renderFooter(DrawContext context, int x, int y, int w, int h, TextRenderer tr) {
        // Ник игрока слева
        String nick = mc.getSession().getUsername();
        context.drawText(tr, nick, x + 16, y + (h - tr.fontHeight) / 2,
                new Color(100, 104, 150).getRGB(), false);
    }

    private void renderModules(DrawContext context, int startX, int startY, int mouseX, int mouseY) {
        int cardH   = moduleElement.getHeight();
        int cardW   = ModuleElement.CARD_WIDTH;
        int padding = ModuleElement.CARD_PADDING;

        int[] colX = new int[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            colX[i] = startX + i * (cardW + padding);
        }

        int[] colY = new int[COLUMNS];
        Arrays.fill(colY, startY);

        int col = 0;
        for (ModuleEntry module : currentModules) {
            int cx = colX[col];
            int cy = colY[col] + (int) scrollOffset;

            moduleElement.render(context, cx, cy, module);

            colY[col] += cardH + padding;
            col = (col + 1) % COLUMNS;
        }
    }

    private void renderScrollBar(DrawContext context, int x, int y, int w, int h) {
        if (maxScroll <= 0) return;

        DrawHelper.drawRect(context, x, y, w, h, new Color(22, 23, 30));

        float ratio     = (float) h / (h + maxScroll);
        int barH        = Math.max(20, (int)(ratio * h));
        float progress  = (-scrollOffset) / maxScroll;
        int barY        = y + (int)(progress * (h - barH));

        DrawHelper.drawRect(context, x, barY, w, barH, new Color(75, 80, 140));
    }

    // ─── Mouse & Key ─────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = menuX();
        int y = menuY();

        // Клик по футеру (категории)
        if (mouseY >= y + MENU_H && mouseY <= y + MENU_H + FOOTER_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                updateCurrentModules();
                return true;
            }
        }

        // Клик по модулям
        if (mouseY >= y && mouseY <= y + MENU_H) {
            if (handleModuleClick(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleModuleClick(double mouseX, double mouseY, int button) {
        int startX = menuX() + 16;
        int startY = menuY() + 16;

        int cardH   = moduleElement.getHeight();
        int cardW   = ModuleElement.CARD_WIDTH;
        int padding = ModuleElement.CARD_PADDING;

        int[] colX = new int[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            colX[i] = startX + i * (cardW + padding);
        }

        int[] colY = new int[COLUMNS];
        Arrays.fill(colY, startY);

        int col = 0;
        for (ModuleEntry module : currentModules) {
            int cx = colX[col];
            int cy = colY[col] + (int) scrollOffset;

            if (moduleElement.mouseClicked(cx, cy, module, mouseX, mouseY, button)) return true;

            colY[col] += cardH + padding;
            col = (col + 1) % COLUMNS;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmt, double vAmt) {
        int x = menuX();
        int y = menuY();

        if (mouseX >= x && mouseX <= x + MENU_W &&
            mouseY >= y && mouseY <= y + MENU_H) {
            scrollVelocity += (float)(vAmt * 18);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, hAmt, vAmt);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Бинды
        for (List<ModuleEntry> list : modulesByCategory.values()) {
            for (ModuleEntry m : list) {
                if (moduleElement.keyPressed(m, keyCode)) return true;
            }
        }

        if (keyCode == 256) { // ESC
            close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ─── Прочее ──────────────────────────────────────────────────────────────

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() { super.close(); }

    // ─── ModuleEntry (заглушка модуля) ───────────────────────────────────────

    public static class ModuleEntry {
        public String  name;
        public boolean enabled          = false;
        public int     bind             = -1;
        public boolean listeningForBind = false;

        public ModuleEntry(String name) {
            this.name = name;
        }
    }
            }
