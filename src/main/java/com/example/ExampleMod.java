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
        // Регистрация кнопки G
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.helper.open", GLFW.GLFW_KEY_G, "category.helper"
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

        // Сканер чата
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String fullText = message.getString();
            
            if (debugMode && MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("§7[SCANNER] Пришло: §f\"" + fullText + "\""), false
                );
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
            if (slot > 8) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
            }
            mc.getNetworkHandler().sendChatCommand("ah sell " + price);
        }
    }

    // Класс экрана
    public static class HelperScreen extends Screen {
        private float animX = -200;

        public HelperScreen() {
            super(Text.literal("Helper Menu"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Плавная анимация вылета
            if (animX < 0) animX += delta * 15;
            if (animX > 0) animX = 0;

            int x = (int) animX;

            // Рисуем фон (черный полупрозрачный)
            context.fill(x + 10, 10, x + 150, height - 10, 0xCC000000);
            context.drawText(client.textRenderer, "§6§lFT HELPER", x + 20, 20, 0xFFFFFF, true);

            // Кнопка Сканера
            int btnColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
            context.fill(x + 20, 40, x + 140, 60, 0x44FFFFFF);
            context.drawText(client.textRenderer, "Сканер: " + (debugMode ? "ВКЛ" : "ВЫКЛ"), x + 25, 46, btnColor, false);

            // Сетка хотбара (3x3 для удобства)
            context.drawText(client.textRenderer, "Выбери предмет:", x + 20, 75, 0xAAAAAA, false);
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                int itemX = x + 25 + (col * 35);
                int itemY = 90 + (row * 35);
                
                ItemStack stack = client.player.getInventory().getStack(i);
                context.fill(itemX, itemY, itemX + 32, itemY + 32, 0x33FFFFFF);
                context.drawItem(stack, itemX + 8, itemY + 8);
                
                if (stack.getName().getString().equals(itemToSell)) {
                    context.drawBorder(itemX, itemY, 32, 32, 0xFF55FF55);
                }
            }
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Клик по сканеру
            if (mouseX > 20 && mouseX < 140 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode;
                return true;
            }

            // Выбор слота
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                int itemX = 25 + (col * 35);
                int itemY = 90 + (row * 35);
                if (mouseX > itemX && mouseX < itemX + 32 && mouseY > itemY && mouseY < itemY + 32) {
                    itemToSell = client.player.getInventory().getStack(i).getName().getString();
                    active = true;
                    client.player.closeScreen(); // Закрываем после выбора
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean shouldPause() { return false; } // Чтобы игра не вставала на паузу
    }
}
