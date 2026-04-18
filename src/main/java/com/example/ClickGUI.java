package com.example.ui;

import com.example.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ClickGUI extends Screen {

    private float animationProgress = 0.0f;

    public ClickGUI() {
        super(Text.literal("Nocturn ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        animationProgress = 0.0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Плавное появление (скорость 0.1f)
        animationProgress = MathHelper.lerp(delta * 0.1f, animationProgress, 1.0f);

        // Затемнение фона
        context.fillGradient(0, 0, this.width, this.height, 0x90000000, 0x90000000);

        context.getMatrices().push();
        // Центрируем и масштабируем для эффекта выплывания
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(animationProgress, animationProgress, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // Настройки панелей
        int panelWidth = 140;
        int panelHeight = 340;
        int padding = 12;
        
        Category[] categories = Category.values();
        int totalWidth = (categories.length * panelWidth) + ((categories.length - 1) * padding);
        int startX = (this.width - totalWidth) / 2;
        int startY = 35;

        for (int i = 0; i < categories.length; i++) {
            Category category = categories[i];
            int currentX = startX + (i * (panelWidth + padding));

            // Рендер основной панели (Deep Navy)
            context.fill(currentX, startY, currentX + panelWidth, startY + panelHeight, 0xAA0F1A2E);

            // Шапка
            context.fill(currentX, startY, currentX + panelWidth, startY + 30, 0xFF13233F);

            // Акцентная полоска (Electric Blue)
            context.fill(currentX, startY + 28, currentX + panelWidth, startY + 30, 0xFF00A8FF);

            // Название категории
            String categoryName = category.name(); 
            int textWidth = this.textRenderer.getWidth(categoryName);
            context.drawTextWithShadow(this.textRenderer, categoryName, currentX + (panelWidth / 2) - (textWidth / 2), startY + 9, 0xFFFFFFFF);

            // Отрисовка модулей
            renderModules(context, currentX, startY + 38, panelWidth, category);
        }

        context.getMatrices().pop();

        // Нижний бар (HUD в меню)
        renderStatusBar(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderModules(DrawContext context, int x, int y, int width, Category category) {
        // Здесь мы оптимизировали выборку через switch, чтобы не забивать render()
        switch (category) {
            case COMBAT -> {
                drawModule(context, x, width, y, "AutoSwap", "...");
                drawModule(context, x, width, y + 18, "AutoTotem", "...");
                drawModule(context, x, width, y + 36, "BackTrack", "...");
                drawModule(context, x, width, y + 54, "Criticals", "...");
                drawModule(context, x, width, y + 72, "CrystalAura", "...");
                drawModule(context, x, width, y + 90, "TriggerBot", "...");
                drawModule(context, x, width, y + 108, "Умные криты", "✓", 0xFF00FF88);
            }
            case MOVEMENT -> {
                drawModule(context, x, width, y, "AirStuck", "...");
                drawModule(context, x, width, y + 18, "Flight", "...");
                drawModule(context, x, width, y + 36, "Jesus", "...");
                drawModule(context, x, width, y + 54, "NoFall", "...");
            }
            case RENDER -> {
                drawModule(context, x, width, y, "Interface", "Client ...");
                drawModule(context, x, width, y + 18, "Esp", "...");
                drawModule(context, x, width, y + 36, "Trails", "✗", 0xFFFF5555);
            }
            // Добавь остальные категории (PLAYER, MISC) по такому же принципу
            default -> drawModule(context, x, width, y, "Empty", "...");
        }
    }

    private void drawModule(DrawContext context, int x, int width, int y, String name, String status) {
        drawModule(context, x, width, y, name, status, 0xFFA0A0A0); // Дефолтный серый статус
    }

    private void drawModule(DrawContext context, int x, int width, int y, String name, String status, int statusColor) {
        context.drawTextWithShadow(this.textRenderer, name, x + 10, y, 0xFFFFFFFF);
        int sWidth = this.textRenderer.getWidth(status);
        context.drawTextWithShadow(this.textRenderer, status, x + width - sWidth - 10, y, statusColor);
    }

    private void renderStatusBar(DrawContext context) {
        int barY = this.height - 25;
        context.fill(0, barY, this.width, this.height, 0xCC0A0F1F);
        
        context.drawTextWithShadow(this.textRenderer, "Нито игроков", 15, barY + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Nocturn", this.width / 2 - 25, barY + 7, 0xFF00A8FF);
        context.drawTextWithShadow(this.textRenderer, "Понеты: 290", this.width - 85, barY + 7, 0xFFFFD700);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
