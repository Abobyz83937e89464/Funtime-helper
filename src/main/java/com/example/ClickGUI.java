package com.nocturn.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {

    private final List<Panel> panels = new ArrayList<>();
    private float animationProgress = 0f;
    private ModuleToggleListener toggleListener;

    // Текстура закруглённой панели (генерируется один раз)
    private static Identifier ROUNDED_PANEL_TEXTURE = null;

    public interface ModuleToggleListener {
        void onToggle(String moduleName, boolean active);
    }

    public ClickGUI(ModuleToggleListener listener) {
        super(Text.literal("Nocturn ClickGUI"));
        this.toggleListener = listener;
        initPanels();
        generateRoundedTexture();
    }

    private void generateRoundedTexture() {
        if (ROUNDED_PANEL_TEXTURE != null) return;
        // Создаём текстуру 64x64 с закруглёнными углами (радиус 8)
        int size = 64;
        int radius = 8;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new java.awt.Color(0xE0, 0x10, 0x18, 0x28)); // тёмно-синий с прозрачностью
        g.fillRoundRect(0, 0, size, size, radius * 2, radius * 2);
        g.dispose();

        // Конвертируем в NativeImage и регистрируем текстуру
        net.minecraft.client.texture.NativeImage nativeImage = new net.minecraft.client.texture.NativeImage(size, size, true);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int argb = img.getRGB(x, y);
                nativeImage.setColor(x, y, argb);
            }
        }
        ROUNDED_PANEL_TEXTURE = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("rounded_panel", new net.minecraft.client.texture.NativeImageBackedTexture(nativeImage));
    }

    private void initPanels() {
        // Combat
        Panel combat = new Panel("Combat", 20, 30, 140);
        addModules(combat, new String[]{
            "AutoSwap", "AutoTotem", "BackTrack", "Criticals", "CrystalAura",
            "ElytraTarget", "HitBox", "NoEntityTrace", "NoFriendDamage",
            "TriggerBot", "Только криты", "Уменьшение криты", "Velocity"
        });
        panels.add(combat);

        // Movement
        Panel movement = new Panel("Movement", 20 + 145, 30, 140);
        addModules(movement, new String[]{
            "AirStuck", "AntiKunger", "ElytraBooster", "ElytraRecast", "Flight",
            "GuiMove", "HighJump", "Jesus", "NoFall", "NoSlow", "NoWeb", "Sneak"
        });
        panels.add(movement);

        // Render
        Panel render = new Panel("Render", 20 + 290, 30, 140);
        addModules(render, new String[]{
            "GlassHands", "HitColor", "HitEffect", "InterFace", "ItemPhysics",
            "JumpCircle", "Nametags", "NoRender", "Particles", "ShulkerViewer", "TNTTimer"
        });
        panels.add(render);

        // Player
        Panel player = new Panel("Player", 20 + 435, 30, 140);
        addModules(player, new String[]{
            "Eagle", "ElytraHelper", "FastBreak", "FreeCam", "KTLeave", "NoDelay",
            "NoPush", "Nuker", "Parkour", "ProjectileHelper", "TapeMouse"
        });
        panels.add(player);

        // Misc
        Panel misc = new Panel("Misc", 20 + 580, 30, 140);
        addModules(misc, new String[]{
            "ItemScroll", "ItemSwapFix", "LeaveTracker", "MineHelper", "NameProtect",
            "NoServerRotation", "Notifications", "Optimizer", "PotionCombiner",
            "SeedInvisible", "SPJoiner", "SRPSpool"
        });
        panels.add(misc);
    }

    private void addModules(Panel panel, String[] names) {
        for (String name : names) {
            panel.addModule(name, false);
        }
    }

    @Override
    protected void init() {
        animationProgress = 0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        animationProgress = MathHelper.lerp(delta * 0.2f, animationProgress, 1.0f);

        // Затемнение фона (без блюра)
        context.fill(0, 0, width, height, 0xAA000000);

        context.getMatrices().push();
        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(animationProgress, animationProgress, 1f);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, textRenderer);
        }

        context.getMatrices().pop();
        renderBottomBar(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBottomBar(DrawContext context) {
        int barY = height - 28;
        context.fill(0, barY, width, height, 0xCC0A0A1A);
        context.fill(0, barY, width, barY + 1, 0xFF2A2A4A);

        String info = "Nocturn | " + (MinecraftClient.getInstance().getCurrentServerEntry() != null ?
                MinecraftClient.getInstance().getCurrentServerEntry().address : "Singleplayer");
        context.drawTextWithShadow(textRenderer, info, 10, barY + 9, 0xAAAAAA);

        int modulesOn = (int) panels.stream().flatMap(p -> p.modules.stream()).filter(m -> m.active).count();
        context.drawTextWithShadow(textRenderer, "Modules: " + modulesOn, width - 100, barY + 9, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) {
            if (panel.handleMouseClick(mouseX, mouseY, button, toggleListener)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Panel panel : panels) {
            if (panel.dragging) {
                panel.x += (int) deltaX;
                panel.y += (int) deltaY;
                panel.x = MathHelper.clamp(panel.x, 0, width - panel.width);
                panel.y = MathHelper.clamp(panel.y, 0, height - 30);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) panel.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (Panel panel : panels) {
            if (mouseX >= panel.x && mouseX <= panel.x + panel.width &&
                mouseY >= panel.y && mouseY <= panel.y + panel.getCurrentHeight()) {
                panel.scroll(vertical);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ==================== ВНУТРЕННИЕ КЛАССЫ ====================

    private static class Panel {
        String title;
        int x, y, width;
        List<Module> modules = new ArrayList<>();
        boolean dragging = false;
        private int scrollOffset = 0;
        private static final int MODULE_HEIGHT = 18;
        private static final int HEADER_HEIGHT = 26;
        private static final int MAX_VISIBLE_HEIGHT = 320;

        Panel(String title, int x, int y, int width) {
            this.title = title;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        void addModule(String name, boolean active) {
            modules.add(new Module(name, active));
        }

        int getCurrentHeight() {
            int totalContent = modules.size() * MODULE_HEIGHT + HEADER_HEIGHT;
            return Math.min(totalContent, MAX_VISIBLE_HEIGHT);
        }

        void render(DrawContext context, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer tr) {
            int panelHeight = getCurrentHeight();
            int contentHeight = modules.size() * MODULE_HEIGHT;
            boolean needsScroll = contentHeight > panelHeight - HEADER_HEIGHT;

            // Рисуем закруглённую панель через текстуру (растягиваем)
            if (ROUNDED_PANEL_TEXTURE != null) {
                context.drawTexture(ROUNDED_PANEL_TEXTURE, x, y, 0, 0, width, panelHeight, 64, 64);
            } else {
                // fallback
                context.fill(x, y, x + width, y + panelHeight, 0xE0101828);
            }

            // Заголовок (полупрозрачный с обводкой)
            context.fill(x, y, x + width, y + HEADER_HEIGHT, 0xB01A1F2E);
            context.fill(x, y + HEADER_HEIGHT - 1, x + width, y + HEADER_HEIGHT, 0xFF5A5A8A);
            context.drawTextWithShadow(tr, title, x + 8, y + 8, 0xFFFFFF);

            // Клиппинг для модулей (скролл)
            int clipY = y + HEADER_HEIGHT;
            int clipHeight = panelHeight - HEADER_HEIGHT;
            int startIdx = Math.max(0, scrollOffset / MODULE_HEIGHT);
            int endIdx = Math.min(modules.size(), startIdx + (clipHeight + MODULE_HEIGHT - 1) / MODULE_HEIGHT + 1);

            for (int i = startIdx; i < endIdx; i++) {
                Module mod = modules.get(i);
                int moduleY = y + HEADER_HEIGHT + (i * MODULE_HEIGHT) - scrollOffset;
                if (moduleY + MODULE_HEIGHT < clipY || moduleY >= clipY + clipHeight) continue;

                boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT;

                // Цвет фона модуля: если активен — темно-серый с оттенком, иначе прозрачный
                if (mod.active) {
                    context.fill(x + 2, moduleY, x + width - 2, moduleY + MODULE_HEIGHT, 0x30204080);
                }
                if (hovered) {
                    context.fill(x + 2, moduleY, x + width - 2, moduleY + MODULE_HEIGHT, 0x40FFFFFF);
                }

                // Полоска слева (статус)
                int statusColor = mod.active ? 0xFF00AAFF : 0xFF555555;
                context.fill(x + 1, moduleY + 2, x + 3, moduleY + MODULE_HEIGHT - 2, statusColor);

                // Имя модуля: белое если активно, светло-серое если нет
                int textColor = mod.active ? 0xFFFFFFFF : 0xFFAAAAAA;
                context.drawTextWithShadow(tr, mod.name, x + 10, moduleY + 5, textColor);
            }

            // Полоса прокрутки
            if (needsScroll) {
                int scrollBarHeight = Math.max(20, (int)((float)clipHeight / contentHeight * clipHeight));
                int scrollBarY = y + HEADER_HEIGHT + (int)((float)scrollOffset / (contentHeight - clipHeight) * (clipHeight - scrollBarHeight));
                context.fill(x + width - 5, scrollBarY, x + width - 2, scrollBarY + scrollBarHeight, 0xFF8888AA);
            }
        }

        boolean handleMouseClick(double mouseX, double mouseY, int button, ModuleToggleListener listener) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_HEIGHT) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    dragging = true;
                }
                return true;
            }

            int panelHeight = getCurrentHeight();
            int clipY = y + HEADER_HEIGHT;
            int clipHeight = panelHeight - HEADER_HEIGHT;
            int startIdx = Math.max(0, scrollOffset / MODULE_HEIGHT);
            int endIdx = Math.min(modules.size(), startIdx + (clipHeight + MODULE_HEIGHT - 1) / MODULE_HEIGHT + 1);

            for (int i = startIdx; i < endIdx; i++) {
                int moduleY = y + HEADER_HEIGHT + (i * MODULE_HEIGHT) - scrollOffset;
                if (moduleY + MODULE_HEIGHT < clipY || moduleY >= clipY + clipHeight) continue;
                if (mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT) {
                    Module mod = modules.get(i);
                    mod.active = !mod.active;
                    if (listener != null) listener.onToggle(mod.name, mod.active);
                    return true;
                }
            }
            return false;
        }

        void scroll(double amount) {
            int contentHeight = modules.size() * MODULE_HEIGHT;
            int panelHeight = getCurrentHeight();
            int clipHeight = panelHeight - HEADER_HEIGHT;
            if (contentHeight <= clipHeight) return;
            scrollOffset = MathHelper.clamp(scrollOffset + (int)(amount * 15), 0, contentHeight - clipHeight);
        }
    }

    private static class Module {
        String name;
        boolean active;
        Module(String name, boolean active) {
            this.name = name;
            this.active = active;
        }
    }
                    }
