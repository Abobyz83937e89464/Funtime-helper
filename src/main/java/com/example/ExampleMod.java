package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding configKey;
    public static String itemToSell = "";
    public static int price = 20000;
    public static boolean active = false;
    public static boolean debugMode = false;
    private int delayTicks = -1;

    @Override
    public void onInitializeClient() {
        // Регистрация кнопки M в категории "FunTime Helper"
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Открыть меню Хелпера", 
                GLFW.GLFW_KEY_M, 
                "FunTime Helper"
        ));

        // Основной цикл проверки нажатия
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (configKey.wasPressed()) {
                // Если не открывается, мы увидим это сообщение в чате
                client.setScreen(new HelperScreen());
            }

            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && active) executeAutoSell(client);
            }
        });

        // Слушатель чата
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String fullText = message.getString();
            if (debugMode && MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§7[DEBUG] " + fullText), false);
            }
            if (active && fullText.toLowerCase().contains("у вас купили")) {
                delayTicks = 40; 
            }
        });
    }

    private void executeAutoSell(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getName().getString().equals(itemToSell)) {
                slot = i; break;
            }
        }
        if (slot != -1) {
            if (slot > 8) mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
            mc.getNetworkHandler().sendChatCommand("ah sell " + price);
        }
    }

    // КЛАСС МЕНЮ С АНИМАЦИЯМИ
    public static class HelperScreen extends Screen {
        private long startTime;
        private final String playerName;

        public HelperScreen() {
            super(Text.literal("Helper Menu"));
            this.startTime = System.currentTimeMillis();
            this.playerName = MinecraftClient.getInstance().getSession().getUsername();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            long elapsed = System.currentTimeMillis() - startTime;
            
            // Плавный темный фон
            float bgAlpha = Math.min(elapsed / 400f, 0.7f);
            context.fill(0, 0, width, height, (int)(bgAlpha * 255) << 24);

            if (elapsed < 1400) {
                // Анимация приветствия
                float textAlpha = (elapsed < 500) ? elapsed / 500f : (elapsed < 1000) ? 1f : 1f - (elapsed - 1000) / 400f;
                int color = ((int)(textAlpha * 255) << 24) | 0xFFFFFF;
                context.drawCenteredTextWithShadow(client.textRenderer, "Привет, " + playerName, width / 2, height / 2 - 10, color);
            } else {
                float menuElapsed = elapsed - 1400;
                
                // Лесенка появления элементов
                renderElement(context, "§6§lFT HELPER", 20, 20, menuElapsed, 0);
                renderButton(context, menuElapsed, 150);
                renderInventory(context, menuElapsed, 300);
            }
            super.render(context, mouseX, mouseY, delta);
        }

        private void renderElement(DrawContext context, String text, int x, int y, float elapsed, int delay) {
            if (elapsed > delay) {
                int animX = (int) Math.min(-100 + ((elapsed - delay) / 300f) * 120, x);
                context.drawText(client.textRenderer, text, animX, y, 0xFFFFFF, true);
            }
        }

        private void renderButton(DrawContext context, float elapsed, int delay) {
            if (elapsed > delay) {
                int animX = (int) Math.min(-150 + ((elapsed - delay) / 300f) * 170, 20);
                int btnColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
                context.fill(animX, 40, animX + 110, 60, 0x44FFFFFF);
                context.drawText(client.textRenderer, "Сканер: " + (debugMode ? "ON" : "OFF"), animX + 5, 46, btnColor, false);
            }
        }

        private void renderInventory(DrawContext context, float elapsed, int delay) {
            if (elapsed > delay) {
                int invX = (int) Math.min(-200 + ((elapsed - delay) / 300f) * 220, 20);
                context.drawText(client.textRenderer, "Предметы:", invX, 75, 0xAAAAAA, false);
                for (int i = 0; i < 9; i++) {
                    int col = i % 3; int row = i / 3;
                    int ix = invX + (col * 35); int iy = 90 + (row * 35);
                    ItemStack stack = client.player.getInventory().getStack(i);
                    context.fill(ix, iy, ix + 32, iy + 32, 0x33FFFFFF);
                    context.drawItem(stack, ix + 8, iy + 8);
                    if (!stack.isEmpty() && stack.getName().getString().equals(itemToSell)) {
                        context.drawBorder(ix, iy, 32, 32, 0xFF55FF55);
                    }
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (System.currentTimeMillis() - startTime < 1400) return false;
            if (mouseX > 20 && mouseX < 130 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode; return true;
            }
            for (int i = 0; i < 9; i++) {
                int ix = 20 + (i % 3 * 35); int iy = 90 + (i / 3 * 35);
                if (mouseX > ix && mouseX < ix + 32 && mouseY > iy && mouseY < iy + 32) {
                    itemToSell = client.player.getInventory().getStack(i).getName().getString();
                    active = true;
                    client.player.closeScreen();
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() { return false; }
    }
}
