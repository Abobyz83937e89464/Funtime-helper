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
    private float maxScroll = 0;

    // Размеры меню
    private static final int MENU_WIDTH = 1100;
    private static final int MENU_HEIGHT = 540;
    private static final int HEADER_HEIGHT = 80;
    private static final int FOOTER_HEIGHT = 80;

    public Menu() {
        super(Text.of("Nocturn Client"));
        this.categoryElement = new CategoryElement();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемнение фона
        super.render(context, mouseX, mouseY, delta);

        MinecraftClient mc = MinecraftClient.getInstance();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int x = (screenWidth - MENU_WIDTH / 2) / 2;
        int y = (screenHeight - MENU_HEIGHT / 2) / 2;

        // Используем масштаб /2 для адаптации
        float scale = 0.5f;
        int menuW = (int)(MENU_WIDTH * scale);
        int menuH = (int)(MENU_HEIGHT * scale);
        int headerH = (int)(HEADER_HEIGHT * scale);
        int footerH = (int)(FOOTER_HEIGHT * scale);

        x = (screenWidth - menuW) / 2;
        y = (screenHeight - menuH) / 2;

        // === HEADER ===
        DrawHelper.drawRect(context, x, y - headerH, menuW, headerH, 10, new Color(29, 31, 44, 204));
        renderHeader(context, x, y - headerH, menuW, headerH);

        // === FOOTER ===
        DrawHelper.drawRect(context, x, y + menuH, menuW, footerH, 10, new Color(29, 31, 44, 204));
        renderFooter(context, x, y + menuH, menuW, footerH);

        // === BACKGROUND ===
        DrawHelper.drawRect(context, x, y, menuW, menuH, 0, new Color(17, 19, 24));

        // === OUTLINE ===
        DrawHelper.drawOutline(context, x - 1, y - headerH - 1, menuW + 2, menuH + headerH + footerH + 2, 10, new Color(52, 51, 64), 1);

        // === КОНТЕНТ (пустая сетка) ===
        renderContent(context, x, y, menuW, menuH);
    }

    private void renderHeader(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String title = "Nocturn Client";
        int titleWidth = textRenderer.getWidth(title);
        int titleX = x + 15;
        int titleY = y + (height - 8) / 2;

        context.drawText(textRenderer, title, titleX, titleY, new Color(197, 200, 255).getRGB(), false);

        // Версия
        String version = "v1.0.0";
        context.drawText(textRenderer, version, x + width - textRenderer.getWidth(version) - 15, titleY, new Color(141, 144, 199).getRGB(), false);
    }

    private void renderFooter(DrawContext context, int x, int y, int width, int height) {
        categoryElement.renderFooter(context, x, y, height);
    }

    private void renderContent(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String categoryName = categoryElement.getSelectedCategory().getDisplayName();
        String placeholder = categoryName + " - No modules yet";

        int textWidth = textRenderer.getWidth(placeholder);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        context.drawText(textRenderer, placeholder, textX, textY, new Color(80, 84, 120).getRGB(), false);

        // Пример сетки модулей (4 пустые карточки)
        int padding = 7;
        int cardWidth = (width - padding * 5) / 4;
        int cardHeight = 60;

        for (int col = 0; col < 4; col++) {
            int cardX = x + padding + col * (cardWidth + padding);
            int cardY = y + 8 + (int) scrollOffset;

            DrawHelper.drawRect(context, cardX, cardY, cardWidth, cardHeight, 3, new Color(28, 29, 38));
            DrawHelper.drawOutline(context, cardX - 1, cardY - 1, cardWidth + 2, cardHeight + 2, 3, new Color(33, 32, 43), 1);

            String cardText = "Module " + (col + 1);
            context.drawText(textRenderer, cardText, cardX + 5, cardY + 5, new Color(127, 133, 172).getRGB(), false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        float scale = 0.5f;
        int menuW = (int)(MENU_WIDTH * scale);
        int menuH = (int)(MENU_HEIGHT * scale);
        int footerH = (int)(FOOTER_HEIGHT * scale);

        int x = (screenWidth - menuW) / 2;
        int y = (screenHeight - menuH) / 2;

        // Клик по footer — категории
        if (mouseY >= y + menuH && mouseY <= y + menuH + footerH) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                scrollOffset = 0;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset += (float)(verticalAmount * 10);
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
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
