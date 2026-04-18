package com.example.ui;

import com.example.Category;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    // Список панелей (по одной на категорию)
    private final List<Panel> panels = new ArrayList<>();
    private float animationProgress = 0.0f;

    public ClickGUI() {
        super(Text.literal("Nocturn ClickGUI"));
        
        // Создаем панели на основе категорий и наполняем их модулями со скрина
        int xOffset = 20;
        for (Category category : Category.values()) {
            Panel panel = new Panel(category.name(), xOffset, 30, 110);
            fillPanelWithModules(panel, category);
            panels.add(panel);
            xOffset += 122; // Компактный отступ между таблицами
        }
    }

    private void fillPanelWithModules(Panel panel, Category category) {
        switch (category) {
            case COMBAT -> {
                panel.addModule("AutoSwap", false); panel.addModule("AutoTotem", true);
                panel.addModule("BackTrack", false); panel.addModule("Criticals", false);
                panel.addModule("CrystalAura", true); panel.addModule("ElytraTarget", false);
                panel.addModule("HitBox", false); panel.addModule("NoEntityTrace", false);
                panel.addModule("NoFriendDamage", false); panel.addModule("TriggerBot", true);
                panel.addModule("Только криты", true); panel.addModule("Умные криты", true);
                panel.addModule("Velocity", false);
            }
            case MOVEMENT -> {
                panel.addModule("AirStuck", false); panel.addModule("AntiHunger", true);
                panel.addModule("ElytraBooster", true); panel.addModule("ElytraRecast", false);
                panel.addModule("Flight", true); panel.addModule("GuiMove", true);
                panel.addModule("HighJump", false); panel.addModule("Jesus", true);
                panel.addModule("NoFall", false); panel.addModule("NoSlow", false);
                panel.addModule("NoWeb", false); panel.addModule("Sneak", false);
            }
            case RENDER -> {
                panel.addModule("GlassHands", false); panel.addModule("HitColor", false);
                panel.addModule("HitEffect", false); panel.addModule("InterFace", true);
                panel.addModule("ItemPhysics", false); panel.addModule("JumpCircle", true);
                panel.addModule("Nametags", false); panel.addModule("NoRender", false);
                panel.addModule("Particles", false); panel.addModule("ShulkerViewer", false);
                panel.addModule("TNTTimer", false); panel.addModule("Trails", false);
            }
            case PLAYER -> {
                panel.addModule("Eagle", false); panel.addModule("ElytraHelper", false);
                panel.addModule("FastBreak", false); panel.addModule("FreeCam", false);
                panel.addModule("KTLeave", false); panel.addModule("NoDelay", false);
                panel.addModule("NoPush", false); panel.addModule("Nuker", false);
                panel.addModule("Parkour", true); panel.addModule("ProjectileHelper", false);
                panel.addModule("TapeMouse", false);
            }
            case MISC -> {
                panel.addModule("ItemScroller", false); panel.addModule("ItemSwapFix", false);
                panel.addModule("LeaveTracker", false); panel.addModule("MineHelper", false);
                panel.addModule("NameProtect", false); panel.addModule("NoServerRotation", false);
                panel.addModule("Notifications", true); panel.addModule("Optimizer", true);
                panel.addModule("PotionCombiner", false); panel.addModule("SeeInvisibles", false);
                panel.addModule("SPJoiner", false); panel.addModule("SRPSpoof", false);
            }
        }
    }

    @Override
    protected void init() {
        animationProgress = 0.0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Анимация появления
        animationProgress = MathHelper.lerp(delta * 0.15f, animationProgress, 1.0f);
        
        // Чистый фон без блюра, просто легкое затемнение
        context.fill(0, 0, this.width, this.height, 0x40000000);

        context.getMatrices().push();
        // Применяем масштаб для всей отрисовки разом
        context.getMatrices().translate(this.width / 2f, this.height / 2f, 0);
        context.getMatrices().scale(animationProgress, animationProgress, 1.0f);
        context.getMatrices().translate(-this.width / 2f, -this.height / 2f, 0);

        // Отрисовка всех панелей
        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, this.textRenderer);
        }

        context.getMatrices().pop();

        // Статус-бар внизу (точно по твоему скрину)
        renderBottomBar(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBottomBar(DrawContext context) {
        int barY = this.height - 24;
        context.fill(0, barY, this.width, this.height, 0xAA0A0F1F);
        
        // Текст статус-бара
        context.drawTextWithShadow(this.textRenderer, "Нито игроков", 15, barY + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "✗", 95, barY + 7, 0xFFFF4444);
        
        context.drawTextWithShadow(this.textRenderer, "Nocturn", this.width / 2 - 25, barY + 7, 0xFF00A8FF);
        
        context.drawTextWithShadow(this.textRenderer, "Понеты:", this.width - 100, barY + 7, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "290", this.width - 50, barY + 7, 0xFFFFD700);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            panel.handleMouseClick(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }

    // --- ВНУТРЕННЯЯ ЛОГИКА ДВИЖКА МЕНЮ ---

    private static class Panel {
        String title;
        int x, y, width;
        List<ModuleUI> modules = new ArrayList<>();
        boolean dragging = false;

        public Panel(String title, int x, int y, int width) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        public void addModule(String name, boolean active) {
            modules.add(new ModuleUI(name, active));
        }

        public void render(DrawContext context, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer tr) {
            // Фон всей панели (темный прозрачный)
            int height = 32 + (modules.size() * 16);
            context.fill(x, y, x + width, y + height, 0x9012192B);
            
            // Заголовок (шапка)
            context.fill(x, y, x + width, y + 22, 0xFF18233D);
            context.fill(x, y + 21, x + width, y + 22, 0xFF00A8FF); // Тонкая полоска внизу шапки
            
            context.drawTextWithShadow(tr, title, x + (width / 2) - (tr.getWidth(title) / 2), y + 7, 0xFFFFFFFF);

            // Отрисовка модулей внутри
            int currentY = y + 28;
            for (ModuleUI mod : modules) {
                mod.render(context, x, currentY, width, mouseX, mouseY, tr);
                currentY += 16;
            }
        }

        public void handleMouseClick(double mouseX, double mouseY, int button) {
            int currentY = y + 28;
            for (ModuleUI mod : modules) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= currentY && mouseY <= currentY + 16) {
                    mod.active = !mod.active; // Переключаем модуль
                    return;
                }
                currentY += 16;
            }
        }
    }

    private static class ModuleUI {
        String name;
        boolean active;

        public ModuleUI(String name, boolean active) {
            this.name = name;
            this.active = active;
        }

        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer tr) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            
            // Подсветка при наведении
            if (hovered) {
                context.fill(x + 2, y, x + width - 2, y + 15, 0x40FFFFFF);
            }

            // Имя модуля (белое если активен, серое если выключен — классика HvH)
            int color = active ? 0xFFFFFFFF : 0xFFA0A0A0;
            context.drawTextWithShadow(tr, name, x + 8, y + 4, color);

            // Иконка статуса справа
            String status = active ? "✓" : "...";
            int sColor = active ? 0xFF00FF88 : 0xFF707070;
            if (name.equals("Velocity") || name.equals("Trails")) { // Красный крестик как на скрине
                status = "✗"; sColor = 0xFFFF4444;
            }

            context.drawTextWithShadow(tr, status, x + width - tr.getWidth(status) - 8, y + 4, sColor);
        }
    }
}
