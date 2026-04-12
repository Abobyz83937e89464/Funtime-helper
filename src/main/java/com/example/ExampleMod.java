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
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.helper.open", GLFW.GLFW_KEY_M, "category.helper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new HelperScreen());
            }
            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && active) executeAutoSell(client);
            }
        });

        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String fullText = message.getString();
            if (debugMode && MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(Text.literal("§7[SCANNER] Пришло: §f\"" + fullText + "\""), false);
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

    // --- КРУТАЯ МЕНЮШКА ---
    public static class HelperScreen extends Screen {
        private long startTime;
        private String playerName;

        public HelperScreen() {
            super(Text.literal("Helper Menu"));
            this.startTime = System.currentTimeMillis();
            this.playerName = MinecraftClient.getInstance().getSession().getUsername();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            long elapsed = System.currentTimeMillis() - startTime;
            
            // 1. Черный фон (плавное появление)
            float bgAlpha = Math.min(elapsed / 500f, 0.8f);
            context.fill(0, 0, width, height, (int)(bgAlpha * 255) << 24);

            // 2. ПРИВЕТСТВИЕ (первые 1.5 секунды)
            if (elapsed < 1500) {
                float textAlpha = 0;
                if (elapsed < 500) textAlpha = elapsed / 500f; // Появление
                else if (elapsed < 1000) textAlpha = 1f; // Удержание
                else textAlpha = 1f - (elapsed - 1000) / 500f; // Исчезновение

                int color = ((int)(textAlpha * 255) << 24) | 0xFFFFFF;
                context.drawCenteredTextWithShadow(client.textRenderer, "Привет, " + playerName, width / 2, height / 2 - 10, color);
            } 
            // 3. ОСНОВНЫЕ ФУНКЦИИ (появляются после 1.2 сек)
            else {
                float menuElapsed = elapsed - 1200;
                
                // Заголовок (выезжает слева)
                int titleX = (int) Math.min(-100 + (menuElapsed / 300f) * 120, 20);
                context.drawText(client.textRenderer, "§6§lFT HELPER", titleX, 20, 0xFFFFFF, true);

                // Кнопка Сканера (лесенка +100мс)
                if (menuElapsed > 100) {
                    int btnX = (int) Math.min(-150 + ((menuElapsed - 100) / 300f) * 170, 20);
                    int btnColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
                    context.fill(btnX, 40, btnX + 120, 60, 0x44FFFFFF);
                    context.drawText(client.textRenderer, "Сканер: " + (debugMode ? "ВКЛ" : "ВЫКЛ"), btnX + 5, 46, btnColor, false);
                }

                // Инвентарь (лесенка +200мс)
                if (menuElapsed > 200) {
                    int invX = (int) Math.min(-200 + ((menuElapsed - 200) / 300f) * 220, 20);
                    context.drawText(client.textRenderer, "Выбери предмет:", invX, 75, 0xAAAAAA, false);
                    
                    for (int i = 0; i < 9; i++) {
                        int row = i / 3;
                        int col = i % 3;
                        int itemX = invX + (col * 35);
                        int itemY = 90 + (row * 35);
                        
                        // Плавное появление самих слотов
                        float itemAlpha = Math.min((menuElapsed - 200 - (i * 50)) / 300f, 1f);
                        if (itemAlpha > 0) {
                            ItemStack stack = client.player.getInventory().getStack(i);
                            context.fill(itemX, itemY, itemX + 32, itemY + 32, (int)(itemAlpha * 51) << 24 | 0xFFFFFF);
                            context.drawItem(stack, itemX + 8, itemY + 8);
                            
                            if (!stack.isEmpty() && stack.getName().getString().equals(itemToSell)) {
                                context.drawBorder(itemX, itemY, 32, 32, 0xFF55FF55);
                            }
                        }
                    }
                }
            }
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < 1200) return false; // Не даем кликать пока идет приветствие

            if (mouseX > 20 && mouseX < 140 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode;
                return true;
            }

            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                int itemX = 20 + (col * 35);
                int itemY = 90 + (row * 35);
                if (mouseX > itemX && mouseX < itemX + 32 && mouseY > itemY && mouseY < itemY + 32) {
                    ItemStack clickedStack = client.player.getInventory().getStack(i);
                    if (!clickedStack.isEmpty()) {
                        itemToSell = clickedStack.getName().getString();
                        active = true;
                        client.player.sendMessage(Text.literal("§a[FT] Цель: " + itemToSell), false);
                        client.player.closeScreen();
                    }
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() { return false; }
    }
}
