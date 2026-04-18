package com.nocturn.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Элегантное меню для Nocturn.
 * Похоже на Vexside: минимализм, анимация, перетаскивание панелей.
 * Не содержит читерской логики — только визуальная оболочка.
 */
public class ClickGUI extends Screen {

    private final List<Panel> panels = new ArrayList<>();
    private float animationProgress = 0f;
    private ModuleToggleListener toggleListener; // твой коллбек для включения/выключения модулей

    // Интерфейс, который ты реализуешь в своём Main-классе
    public interface ModuleToggleListener {
        void onToggle(String moduleName, boolean active);
    }

    public ClickGUI(ModuleToggleListener listener) {
        super(Text.literal("Nocturn ClickGUI"));
        this.toggleListener = listener;
        initPanels();
    }

    private void initPanels() {
        // Здесь ты сам добавишь панели с нужными модулями.
        // Пример для демонстрации. Замени на свои категории и модули.
        Panel combat = new Panel("Combat", 20, 30, 120);
        combat.addModule("AutoClicker", false);
        combat.addModule("Reach", true);
        combat.addModule("Velocity", false);
        panels.add(combat);

        Panel movement = new Panel("Movement", 20 + 125, 30, 120);
        movement.addModule("Sprint", true);
        movement.addModule("Flight", false);
        movement.addModule("NoFall", false);
        panels.add(movement);

        Panel render = new Panel("Render", 20 + 250, 30, 120);
        render.addModule("ESP", false);
        render.addModule("FullBright", true);
        render.addModule("Chams", false);
        panels.add(render);

        // Ты можешь добавлять сколько угодно панелей и модулей
        // Все названия — просто строки, никакой логики читов внутри нет.
    }

    @Override
    protected void init() {
        animationProgress = 0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Анимация появления (scale)
        animationProgress = MathHelper.lerp(delta * 0.15f, animationProgress, 1.0f);

        // Затемнённый фон (без блюра)
        context.fill(0, 0, width, height, 0xAA000000);

        context.getMatrices().push();
        // Центрируем анимацию масштабирования
        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(animationProgress, animationProgress, 1f);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        // Отрисовка всех панелей
        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, textRenderer);
        }

        context.getMatrices().pop();

        // Нижний статус-бар (как в твоём примере, но более чистый)
        renderBottomBar(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBottomBar(DrawContext context) {
        int barY = height - 26;
        context.fill(0, barY, width, height, 0xCC0A0A1A);
        context.fill(0, barY, width, barY + 1, 0xFF2A2A4A);

        String info = "Nocturn | " + MinecraftClient.getInstance().getCurrentServerEntry() != null ?
                MinecraftClient.getInstance().getCurrentServerEntry().address : "Singleplayer";
        context.drawTextWithShadow(textRenderer, info, 10, barY + 8, 0xAAAAAA);

        int modulesOn = (int) panels.stream().flatMap(p -> p.modules.stream()).filter(m -> m.active).count();
        context.drawTextWithShadow(textRenderer, "Modules: " + modulesOn, width - 100, barY + 8, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            if (panel.handleMouseClick(mouseX, mouseY, button, toggleListener)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Panel panel : panels) {
            if (panel.dragging) {
                panel.x += (int) deltaX;
                panel.y += (int) deltaY;
                // Ограничения, чтобы панель не улетела за экран
                panel.x = MathHelper.clamp(panel.x, 0, width - panel.width);
                panel.y = MathHelper.clamp(panel.y, 0, height - 30);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            panel.dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // ---------- ВНУТРЕННИЙ КЛАСС ПАНЕЛИ ----------
    private static class Panel {
        String title;
        int x, y, width;
        List<ModuleButton> modules = new ArrayList<>();
        boolean dragging = false;
        private int scrollOffset = 0;
        private static final int MODULE_HEIGHT = 18;
        private static final int HEADER_HEIGHT = 24;

        public Panel(String title, int x, int y, int width) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        public void addModule(String name, boolean active) {
            modules.add(new ModuleButton(name, active));
        }

        public void render(DrawContext context, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer tr) {
            // Максимальная высота панели (ограничим 300px, дальше скролл)
            int visibleHeight = Math.min(modules.size() * MODULE_HEIGHT + HEADER_HEIGHT, 300);
            int contentHeight = modules.size() * MODULE_HEIGHT;
            boolean needsScroll = contentHeight > visibleHeight - HEADER_HEIGHT;

            // Фон панели
            context.fill(x, y, x + width, y + visibleHeight, 0xE0101828);
            // Обводка
            context.fill(x, y, x + width, y + 1, 0xFF3A3A6A);
            context.fill(x, y + visibleHeight - 1, x + width, y + visibleHeight, 0xFF3A3A6A);

            // Заголовок (перетаскиваемый)
            context.fill(x, y, x + width, y + HEADER_HEIGHT, 0xFF1A1F2E);
            context.fill(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, 0xFF5A5A8A);
            context.drawTextWithShadow(tr, title, x + 8, y + 7, 0xFFFFFF);

            // Область скролла
            int clipY = y + HEADER_HEIGHT;
            int clipHeight = visibleHeight - HEADER_HEIGHT;
            // Отрисовка модулей с учётом скролла
            int startIdx = scrollOffset / MODULE_HEIGHT;
            int endIdx = Math.min(modules.size(), startIdx + (clipHeight + MODULE_HEIGHT - 1) / MODULE_HEIGHT);

            for (int i = startIdx; i < endIdx; i++) {
                ModuleButton mod = modules.get(i);
                int moduleY = y + HEADER_HEIGHT + (i * MODULE_HEIGHT) - scrollOffset;
                if (moduleY + MODULE_HEIGHT > y + visibleHeight) continue;
                if (moduleY + MODULE_HEIGHT < y + HEADER_HEIGHT) continue;

                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT;
                // Фон модуля при наведении
                if (hovered) {
                    context.fill(x + 1, moduleY, x + width - 1, moduleY + MODULE_HEIGHT, 0x30FFFFFF);
                }

                // Имя модуля
                int color = mod.active ? 0xFFFFFFFF : 0xFFAAAAAA;
                context.drawTextWithShadow(tr, mod.name, x + 8, moduleY + 5, color);

                // Иконка статуса (галочка / крестик)
                String statusIcon = mod.active ? "✔" : "✖";
                int iconColor = mod.active ? 0xFF55FF55 : 0xFFFF5555;
                context.drawTextWithShadow(tr, statusIcon, x + width - 12, moduleY + 5, iconColor);
            }

            // Полоса прокрутки, если нужно
            if (needsScroll) {
                int scrollBarHeight = Math.max(20, (int)((float)clipHeight / contentHeight * clipHeight));
                int scrollBarY = y + HEADER_HEIGHT + (int)((float)scrollOffset / (contentHeight - clipHeight) * (clipHeight - scrollBarHeight));
                context.fill(x + width - 4, scrollBarY, x + width - 1, scrollBarY + scrollBarHeight, 0xFF8888AA);
            }
        }

        public boolean handleMouseClick(double mouseX, double mouseY, int button, ModuleToggleListener listener) {
            // Проверка на клик в заголовок для перетаскивания
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    dragging = true;
                    return true;
                }
            }

            // Клик по модулям
            int visibleHeight = Math.min(modules.size() * MODULE_HEIGHT + HEADER_HEIGHT, 300);
            int clipHeight = visibleHeight - HEADER_HEIGHT;
            int startIdx = scrollOffset / MODULE_HEIGHT;

            for (int i = startIdx; i < modules.size(); i++) {
                int moduleY = y + HEADER_HEIGHT + (i * MODULE_HEIGHT) - scrollOffset;
                if (moduleY > y + visibleHeight) break;
                if (moduleY + MODULE_HEIGHT < y + HEADER_HEIGHT) continue;

                if (mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT) {
                    ModuleButton mod = modules.get(i);
                    mod.active = !mod.active;
                    if (listener != null) {
                        listener.onToggle(mod.name, mod.active);
                    }
                    return true;
                }
            }

            // Скролл колесиком обрабатывается в методе mouseScrolled
            return false;
        }

        public void scroll(double amount) {
            int contentHeight = modules.size() * MODULE_HEIGHT;
            int visibleHeight = Math.min(modules.size() * MODULE_HEIGHT + HEADER_HEIGHT, 300) - HEADER_HEIGHT;
            if (contentHeight <= visibleHeight) return;
            scrollOffset = MathHelper.clamp(scrollOffset + (int)(amount * 15), 0, contentHeight - visibleHeight);
        }
    }

    // ---------- КЛАСС МОДУЛЯ (ТОЛЬКО ДАННЫЕ) ----------
    private static class ModuleButton {
        String name;
        boolean active;

        ModuleButton(String name, boolean active) {
            this.name = name;
            this.active = active;
        }
    }

    // Обработка скролла внутри GUI
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Panel panel : panels) {
            if (mouseX >= panel.x && mouseX <= panel.x + panel.width &&
                mouseY >= panel.y && mouseY <= panel.y + Math.min(panel.modules.size() * 18 + 24, 300)) {
                panel.scroll(verticalAmount);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
