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
            if (client.player == null) return;
            while (configKey.wasPressed()) {
                client.setScreen(new HelperScreen());
            }
            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && active) executeAutoSell(client);
            }
        });

        // 1. ЛОВИМ ОБЫЧНЫЙ ЧАТ (игроки)
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            processLog(message.getString(), "ЧАТ");
        });

        // 2. ЛОВИМ СИСТЕМНЫЕ СООБЩЕНИЯ (Аукцион, Сервер - то, что нам нужно!)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            processLog(message.getString(), "СИСТЕМА");
        });
    }

    // Обработчик сообщений
    private void processLog(String fullText, String type) {
        // Логируем КАЖДОЕ сообщение, если сканер включен
        if (debugMode && MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("§8[ЛОГ-" + type + "]: §f" + fullText), false);
        }

        // Логика самой авто-продажи
        if (active && fullText.toLowerCase().contains("у вас купили")) {
            delayTicks = 40; 
        }
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

    // --- НАШЕ МЕНЮ ---
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
            
            float bgAlpha = Math.min(elapsed / 400f, 0.7f);
            context.fill(0, 0, 170, height, (int)(bgAlpha * 255) << 24);

            if (elapsed < 1400) {
                float textAlpha = (elapsed < 500) ? elapsed / 500f : (elapsed < 1000) ? 1f : 1f - (elapsed - 1000) / 400f;
                int color = ((int)(textAlpha * 255) << 24) | 0xFFFFFF;
                // Приветствие по центру левой панели
                context.drawCenteredTextWithShadow(client.textRenderer, "Привет, " + playerName, 85, height / 2 - 10, color);
            } else {
                context.drawText(client.textRenderer, "§6§lFT HELPER", 20, 20, 0xFFFFFF, true);
                
                int btnColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
                context.fill(20, 40, 140, 60, 0x44FFFFFF);
                context.drawText(client.textRenderer, "Сканер: " + (debugMode ? "ВКЛ" : "ВЫКЛ"), 25, 46, btnColor, false);

                context.drawText(client.textRenderer, "Выбери:", 20, 75, 0xAAAAAA, false);
                for (int i = 0; i < 9; i++) {
                    int ix = 20 + (i % 3 * 35); int iy = 90 + (i / 3 * 35);
                    ItemStack stack = client.player.getInventory().getStack(i);
                    context.fill(ix, iy, ix + 32, iy + 32, 0x33FFFFFF);
                    context.drawItem(stack, ix + 8, iy + 8);
                    if (!stack.isEmpty() && stack.getName().getString().equals(itemToSell)) {
                        context.drawBorder(ix, iy, 32, 32, 0xFF55FF55);
                    }
                }
            }
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Клик по сканеру
            if (mouseX > 20 && mouseX < 140 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode;
                client.player.sendMessage(Text.literal("§eСканер теперь: " + (debugMode ? "§aВКЛЮЧЕН" : "§cВЫКЛЮЧЕН")), false);
                return true;
            }
            // Клик по предмету
            for (int i = 0; i < 9; i++) {
                int ix = 20 + (i % 3 * 35); int iy = 90 + (i / 3 * 35);
                if (mouseX > ix && mouseX < ix + 32 && mouseY > iy && mouseY < iy + 32) {
                    ItemStack clickedStack = client.player.getInventory().getStack(i);
                    if (!clickedStack.isEmpty()) {
                        itemToSell = clickedStack.getName().getString();
                        active = true;
                        // ПИШЕТ В ЧАТ ВЫБРАННЫЙ ПРЕДМЕТ
                        client.player.sendMessage(Text.literal("§aВыбрано: §e" + itemToSell), false);
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
