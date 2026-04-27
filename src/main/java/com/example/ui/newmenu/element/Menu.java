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

    private static final int MENU_WIDTH = 320;
    private static final int MENU_HEIGHT = 180;
    private static final int HEADER_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 22;

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

        // === OUTLINE ===
        DrawHelper.drawOutline(context,
                x - 1, y - HEADER_HEIGHT - 1,
                MENU_WIDTH + 2, HEADER_HEIGHT + MENU_HEIGHT + FOOTER_HEIGHT + 2,
                0, new Color(52, 51, 64), 1);

        // === КОНТЕНТ ===
        renderContent(context, x, y, MENU_WIDTH, MENU_HEIGHT);
    }

    private void renderHeader(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        String title = "Nocturn Client";
        int titleX = x + 6;
        int titleY = y + (height - 8) / 2;
        context.drawText(textRenderer, title, titleX, titleY, new Color(197, 200, 255).getRGB(), false);

        String version = "v1.0.0";
        int versionX = x + width - textRenderer.getWidth(version) - 6;
        context.drawText(textRenderer, version, versionX, titleY, new Color(141, 144, 199).getRGB(), false);
    }

    private void renderFooter(DrawContext context, int x, int y, int width, int height) {
        categoryElement.renderFooter(context, x, y, width, height);
    }

    private void renderContent(DrawContext context, int x, int y, int width, int height) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int padding = 4;
        int columns = 4;
        int cardWidth = (width - padding * (columns + 1)) / columns;
        int cardHeight = 35;
        int rows = 4;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int cardX = x + padding + col * (cardWidth + padding);
                int cardY = y + padding + row * (cardHeight + padding) + (int) scrollOffset;

                if (cardY + cardHeight < y || cardY > y + height) continue;

                DrawHelper.drawRect(context, cardX, cardY, cardWidth, cardHeight, 3,
                        new Color(28, 29, 38));
                DrawHelper.drawOutline(context, cardX - 1, cardY - 1,
                        cardWidth + 2, cardHeight + 2, 3,
                        new Color(33, 32, 43), 1);

                String moduleName = "Module";
                context.drawText(textRenderer, moduleName, cardX + 4, cardY + 4,
                        new Color(127, 133, 172).getRGB(), false);

                String enabled = "OFF";
                context.drawText(textRenderer, enabled, cardX + 4, cardY + 16,
                        new Color(80, 84, 120).getRGB(), false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getMenuX();
        int y = getMenuY();

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
            scrollOffset += (float) (verticalAmount * 10);
            if (scrollOffset > 0) scrollOffset = 0;
            float maxScroll = Math.max(0, 4 * 39 - MENU_HEIGHT + 8);
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
