package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Menu extends Screen {

    private final CategoryElement categoryElement;
    private float scrollOffset = 0;
    private float maxScroll = 0;

    // Размеры меню (пиксели на экране)
    private static final int MENU_WIDTH = 480;
    private static final int MENU_HEIGHT = 260;
    private static final int HEADER_HEIGHT = 36;
    private static final int FOOTER_HEIGHT = 36;

    // Карточки модулей
    private static final int COLUMNS = 4;
    private static final int CARD_PADDING = 6;
    private static final int CARD_HEIGHT = 52;

    // Заглушки модулей для каждой категории
    private final List<ModuleEntry> currentModules = new ArrayList<>();

    public Menu() {
        super(Text.of("Nocturn Client"));
        this.categoryElement = new CategoryElement();
        loadModulesForCategory();
    }

    private void loadModulesForCategory() {
        currentModules.clear();
        String cat = categoryElement.getSelectedCategory().getDisplayName();

        // Заглушки — потом заменишь на реальные модули
        switch (categoryElement.getSelectedCategory()) {
            case COMBAT -> {
                currentModules.add(new ModuleEntry("KillAura", false));
                currentModules.add(new ModuleEntry("Velocity", false));
                currentModules.add(new ModuleEntry("AutoTotem", true));
                currentModules.add(new ModuleEntry("Criticals", false));
                currentModules.add(new ModuleEntry("Reach", false));
                currentModules.add(new ModuleEntry("AimAssist", false));
                currentModules.add(new ModuleEntry("AutoPot", false));
                currentModules.add(new ModuleEntry("AntiBot", true));
            }
            case MOVEMENT -> {
                currentModules.add(new ModuleEntry("Speed", false));
                currentModules.add(new ModuleEntry("Fly", false));
                currentModules.add(new ModuleEntry("Sprint", true));
                currentModules.add(new ModuleEntry("NoSlow", false));
                currentModules.add(new ModuleEntry("Step", false));
                currentModules.add(new ModuleEntry("Strafe", false));
            }
            case RENDER -> {
                currentModules.add(new ModuleEntry("ESP", false));
                currentModules.add(new ModuleEntry("Tracers", false));
                currentModules.add(new ModuleEntry("NameTags", true));
                currentModules.add(new ModuleEntry("Fullbright", true));
                currentModules.add(new ModuleEntry("NoRender", false));
            }
            case PLAYER -> {
                currentModules.add(new ModuleEntry("AutoArmor", false));
                currentModules.add(new ModuleEntry("ChestStealer", false));
                currentModules.add(new ModuleEntry("FastPlace", true));
                currentModules.add(new ModuleEntry("NoFall", false));
            }
            case WORLD -> {
                currentModules.add(new ModuleEntry("Scaffold", false));
                currentModules.add(new ModuleEntry("Timer", false));
                currentModules.add(new ModuleEntry("Nuker", false));
            }
            case MISC -> {
                currentModules.add(new ModuleEntry("AutoFish", false));
                currentModules.add(new ModuleEntry("Disabler", false));
                currentModules.add(new ModuleEntry("AntiCheat", false));
                currentModules.add(new ModuleEntry("Spammer", false));
            }
        }
        scrollOffset = 0;
        updateMaxScroll();
    }

    private void updateMaxScroll() {
        int rows = (int) Math.ceil((double) currentModules.size() / COLUMNS);
        int contentHeight = rows * (CARD_HEIGHT + CARD_PADDING) + CARD_PADDING;
        maxScroll = Math.max(0, contentHeight - MENU_HEIGHT + 16);
    }

    private int getMenuX() {
        return (MinecraftClient.getInstance().getWindow().getScaledWidth() - MENU_WIDTH) / 2;
    }

    private int getMenuY() {
        return (MinecraftClient.getInstance().getWindow().getScaledHeight() - MENU_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = getMenuX();
        int y = getMenuY();

        // === Затемнение всего экрана ===
        DrawHelper.drawRect(context, 0, 0,
                MinecraftClient.getInstance().getWindow().getScaledWidth(),
                MinecraftClient.getInstance().getWindow().getScaledHeight(),
                new Color(0, 0, 0, 120));

        // === Тень вокруг меню ===
        DrawHelper.drawShadow(context, x, y - HEADER_HEIGHT, MENU_WIDTH, HEADER_HEIGHT + MENU_HEIGHT + FOOTER_HEIGHT, 6);

        // === HEADER ===
        DrawHelper.drawRect(context, x, y - HEADER_HEIGHT, MENU_WIDTH, HEADER_HEIGHT,
                new Color(29, 31, 44));
        // Линия-разделитель под header
        DrawHelper.drawRect(context, x, y - 1, MENU_WIDTH, 1,
                new Color(52, 51, 64));
        renderHeader(context, x, y - HEADER_HEIGHT, MENU_WIDTH, HEADER_HEIGHT);

        // === MAIN BACKGROUND ===
        DrawHelper.drawRect(context, x, y, MENU_WIDTH, MENU_HEIGHT,
                new Color(17, 19, 24));

        // === CONTENT (модули в сетке) ===
        renderContent(context, x, y, MENU_WIDTH, MENU_HEIGHT, mouseX, mouseY);

        // === Линия-разделитель над footer ===
        DrawHelper.drawRect(context, x, y + MENU_HEIGHT, MENU_WIDTH, 1,
                new Color(52, 51, 64));

        // === FOOTER ===
        DrawHelper.drawRect(context, x, y + MENU_HEIGHT + 1, MENU_WIDTH, FOOTER_HEIGHT - 1,
                new Color(29, 31, 44));
        categoryElement.render(context, x, y + MENU_HEIGHT + 1, MENU_WIDTH, FOOTER_HEIGHT - 1);

        // === OUTLINE вокруг всего ===
        DrawHelper.drawOutlineRect(context,
                x - 1, y - HEADER_HEIGHT - 1,
                MENU_WIDTH + 2, HEADER_HEIGHT + MENU_HEIGHT + FOOTER_HEIGHT + 2,
                new Color(52, 51, 64), 1);
    }

    private void renderHeader(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // Название клиента
        String title = "Nocturn Client";
        int titleX = x + 10;
        int titleY = y + (height - 8) / 2;

        // Фоновая подсветка текста названия
        int titleWidth = textRenderer.getWidth(title);
        DrawHelper.drawRect(context, titleX - 4, titleY - 3, titleWidth + 8, 14,
                new Color(51, 56, 94, 80));

        context.drawText(textRenderer, title, titleX, titleY, new Color(197, 200, 255).getRGB(), false);

        // Версия справа
        String version = "v1.0.0";
        int versionX = x + width - textRenderer.getWidth(version) - 10;
        context.drawText(textRenderer, version, versionX, titleY, new Color(100, 104, 150).getRGB(), false);

        // Категория по центру
        String categoryName = categoryElement.getSelectedCategory().getDisplayName();
        int catWidth = textRenderer.getWidth(categoryName);
        int catX = x + (width - catWidth) / 2;
        context.drawText(textRenderer, categoryName, catX, titleY, new Color(141, 144, 199).getRGB(), false);
    }

    private void renderContent(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int cardWidth = (width - CARD_PADDING * (COLUMNS + 1)) / COLUMNS;

        // Включаем scissor чтобы контент не выходил за пределы
        context.enableScissor(x, y, x + width, y + height);

        int startY = y + CARD_PADDING + (int) scrollOffset;

        for (int i = 0; i < currentModules.size(); i++) {
            ModuleEntry module = currentModules.get(i);

            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int cardX = x + CARD_PADDING + col * (cardWidth + CARD_PADDING);
            int cardY = startY + row * (CARD_HEIGHT + CARD_PADDING);

            // Пропускаем невидимые карточки
            if (cardY + CARD_HEIGHT < y || cardY > y + height) continue;

            boolean hovered = mouseX >= cardX && mouseX <= cardX + cardWidth &&
                              mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT &&
                              mouseY >= y && mouseY <= y + height;

            // === Карточка модуля ===
            Color cardBg = hovered ? new Color(33, 35, 48) : new Color(28, 29, 38);
            DrawHelper.drawRect(context, cardX, cardY, cardWidth, CARD_HEIGHT, cardBg);

            // Outline карточки
            Color outlineColor = module.enabled ? new Color(75, 80, 140) : new Color(38, 39, 52);
            DrawHelper.drawOutlineRect(context, cardX, cardY, cardWidth, CARD_HEIGHT, outlineColor, 1);

            // Акцентная полоска сверху если включён
            if (module.enabled) {
                DrawHelper.drawRect(context, cardX + 1, cardY + 1, cardWidth - 2, 2,
                        new Color(125, 136, 255));
            }

            // Название модуля
            int textY = cardY + 8;
            Color nameColor = module.enabled ? new Color(197, 200, 255) : new Color(127, 133, 172);
            context.drawText(textRenderer, module.name, cardX + 8, textY, nameColor.getRGB(), false);

            // Статус ON/OFF
            String status = module.enabled ? "ON" : "OFF";
            Color statusColor = module.enabled ? new Color(100, 220, 100) : new Color(80, 84, 120);
            context.drawText(textRenderer, status, cardX + 8, textY + 14, statusColor.getRGB(), false);

            // Индикатор справа
            int indicatorSize = 8;
            int indicatorX = cardX + cardWidth - indicatorSize - 8;
            int indicatorY = cardY + (CARD_HEIGHT - indicatorSize) / 2;
            Color indicatorColor = module.enabled ? new Color(125, 136, 255) : new Color(45, 47, 60);
            DrawHelper.drawRect(context, indicatorX, indicatorY, indicatorSize, indicatorSize, indicatorColor);
            DrawHelper.drawOutlineRect(context, indicatorX, indicatorY, indicatorSize, indicatorSize,
                    new Color(60, 62, 80), 1);

            // Бинд (заглушка)
            String bind = "[NONE]";
            int bindWidth = textRenderer.getWidth(bind);
            context.drawText(textRenderer, bind, cardX + cardWidth - bindWidth - 8, textY + 14 + 12,
                    new Color(60, 63, 90).getRGB(), false);
        }

        context.disableScissor();

        // Полоса прокрутки
        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, (int) ((float) height / (height + maxScroll) * height));
            float scrollProgress = -scrollOffset / maxScroll;
            int scrollBarY = y + (int) (scrollProgress * (height - scrollBarHeight));
            int scrollBarX = x + width - 3;

            DrawHelper.drawRect(context, scrollBarX, y, 2, height, new Color(25, 26, 35));
            DrawHelper.drawRect(context, scrollBarX, scrollBarY, 2, scrollBarHeight, new Color(75, 80, 140));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getMenuX();
        int y = getMenuY();

        // Клик по footer — категории
        if (mouseY >= y + MENU_HEIGHT + 1 && mouseY <= y + MENU_HEIGHT + FOOTER_HEIGHT) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                loadModulesForCategory();
                return true;
            }
        }

        // Клик по карточкам модулей
        if (mouseY >= y && mouseY <= y + MENU_HEIGHT) {
            if (handleModuleClick(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleModuleClick(double mouseX, double mouseY, int button) {
        int x = getMenuX();
        int y = getMenuY();
        int cardWidth = (MENU_WIDTH - CARD_PADDING * (COLUMNS + 1)) / COLUMNS;
        int startY = y + CARD_PADDING + (int) scrollOffset;

        for (int i = 0; i < currentModules.size(); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            int cardX = x + CARD_PADDING + col * (cardWidth + CARD_PADDING);
            int cardY = startY + row * (CARD_HEIGHT + CARD_PADDING);

            if (mouseX >= cardX && mouseX <= cardX + cardWidth &&
                mouseY >= cardY && mouseY <= cardY + CARD_HEIGHT) {

                if (button == 0) {
                    // ЛКМ — toggle модуля
                    currentModules.get(i).enabled = !currentModules.get(i).enabled;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int x = getMenuX();
        int y = getMenuY();

        if (mouseX >= x && mouseX <= x + MENU_WIDTH &&
            mouseY >= y && mouseY <= y + MENU_HEIGHT) {
            scrollOffset += (float) (verticalAmount * 20);
            if (scrollOffset > 0) scrollOffset = 0;
            if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Простая заглушка для модуля
    public static class ModuleEntry {
        public String name;
        public boolean enabled;

        public ModuleEntry(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }
    }
                }
