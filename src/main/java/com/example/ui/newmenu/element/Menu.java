package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

public class Menu extends Screen {

    private final CategoryElement categoryElement;
    private float scrollOffset = 0;

    private static final int MENU_WIDTH = 550;
    private static final int MENU_HEIGHT = 270;
    private static final int HEADER_HEIGHT = 40;
    private static final int FOOTER_HEIGHT = 40;

    public Menu() {
        super(Text.of("Nocturn Client"));
        this.categoryElement = new CategoryElement();
    }

    private int getMenuX() {
        return (MinecraftClient.getInstance().getWindow().getScaledWidth() - MENU_WIDTH) / 2;
    }

    private int getMenuY() {
        return (MinecraftClient.getInstance().getWindow().getScaledHeight() - MENU_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = getMenuX();
        int y = getMenuY();

        // === HEADER ===
        DrawHelper.drawRect(context, x, y - HEADER_HEIGHT, MENU_WIDTH, HEADER_HEIGHT, 0,
                new Color(29, 31, 44, 204));
        renderHeader(context, x, y - HEADER_HEIGHT, MENU_WIDTH, HEADER_HEIGHT);

        // === BACKGROUND ===
        DrawHelper.drawRect(context, x, y, MENU_WIDTH, MENU_HEIGHT, 0,
                new Color(17, 19, 24));

        // === FOOTER ===
        DrawHelper.drawRect(context, x, y + MENU_HEIGHT, MENU_WIDTH, FOOTER_HEIGHT, 0,
                new Color(29, 31, 44, 204));
        renderFooter(context, x, y + MENU_HEIGHT, MENU_WIDTH, FOOTER_HEIGHT);

        // === OUTLINE вокруг всего ===
        DrawHelper.drawOutline(context,
                x - 2, y - HEADER_HEIGHT - 2,
                MENU_WIDTH + 4, HEADER_HEIGHT + MENU_HEIGHT + FOOTER_HEIGHT + 4,
                0, new Color(52, 51, 64), 2);

        // === КОНТЕНТ ===
        renderContent(context, x, y, MENU_WIDTH, MENU_HEIGHT);
    }

    private void renderHeader(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String title = "Nocturn Client";
        int titleX = x + 12;
        int titleY = y + (height - 8) / 2;
        context.drawText(textRenderer, title, titleX, titleY, new Color(197, 200, 255).getRGB(), false);

        String version = "v1.0.0";
        int versionX = x + width - textRenderer.getWidth(version) - 12;
        context.drawText(textRenderer, version, versionX, titleY, new Color(141, 144, 199).getRGB(), false);
    }

    private void renderFooter(DrawContext context, int x, int y, int width, int height) {
        categoryElement.renderFooter(context, x, y, width, height);
    }

    private void renderContent(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int padding = 8;
        int columns = 4;
        int cardWidth = (width - padding * (columns + 1)) / columns;
        int cardHeight = 50;
        int rows = 3;

        String categoryName = categoryElement.getSelectedCategory().getDisplayName();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int cardX = x + padding + col * (cardWidth + padding);
                int cardY = y + padding + row * (cardHeight + padding) + (int) scrollOffset;

                // Проверка видимости
                if (cardY + cardHeight < y || cardY > y + height) continue;

                // Карточка модуля
                DrawHelper.drawRect(context, cardX, cardY, cardWidth, cardHeight, 3,
                        new Color(28, 29, 38));
                DrawHelper.drawOutline(context, cardX - 1, cardY - 1,
                        cardWidth + 2, cardHeight + 2, 3,
                        new Color(33, 32, 43), 1);

                // Заглушка текста
                String moduleName = categoryName + " " + (row * columns + col + 1);
                context.drawText(textRenderer, moduleName, cardX + 8, cardY + 6,
                        new Color(127, 133, 172).getRGB(), false);

                // Индикатор "Enabled"
                String enabled = "OFF";
                context.drawText(textRenderer, enabled, cardX + 8, cardY + 20,
                        new Color(80, 84, 120).getRGB(), false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getMenuX();
        int y = getMenuY();

        // Footer клик
        if (mouseY >= y + MENU_HEIGHT && mouseY <= y + MENU_HEIGHT + FOOTER_HEIGHT) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                scrollOffset = 0;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int x = getMenuX();
        int y = getMenuY();

        if (mouseX >= x && mouseX <= x + MENU_WIDTH &&
            mouseY >= y && mouseY <= y + MENU_HEIGHT) {
            scrollOffset += (float) (verticalAmount * 15);
            if (scrollOffset > 0) scrollOffset = 0;
            float maxScroll = Math.max(0, 3 * 58 - MENU_HEIGHT + 16);
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
}
