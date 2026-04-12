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

        // Безопасный перехват сообщений
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String text = message.getString();
            MinecraftClient client = MinecraftClient.getInstance();
            
            client.execute(() -> {
                if (debugMode && client.player != null) {
                    client.player.sendMessage(Text.literal("§8[LOG]: §f" + text), false);
                }
                if (active && text.toLowerCase().contains("у вас купили")) {
                    delayTicks = 40;
                }
            });
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
            
            // НИКАКИХ ФОНОВ И БЛЮРА. Только чистый текст и элементы.

            if (elapsed < 1200) {
                // Плавное приветствие без подложки
                float textAlpha = (elapsed < 400) ? elapsed / 400f : (elapsed < 800) ? 1f : 1f - (elapsed - 800) / 400f;
                int color = ((int)(textAlpha * 255) << 24) | 0xFFFFFF;
                context.drawCenteredTextWithShadow(client.textRenderer, "Привет, " + playerName, 80, height / 2, color);
            } else {
                // Основной интерфейс
                context.drawText(client.textRenderer, "§6§lFT HELPER", 20, 20, 0xFFFFFF, true);
                
                // Кнопка сканера
                int btnColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
                context.fill(20, 40, 140, 60, 0x66000000); // Полупрозрачная черная рамка для читаемости
                context.drawText(client.textRenderer, "Сканер: " + (debugMode ? "ВКЛ" : "ВЫКЛ"), 25, 46, btnColor, false);

                // Инвентарь
                context.drawText(client.textRenderer, "Выбери предмет:", 20, 75, 0xAAAAAA, false);
                for (int i = 0; i < 9; i++) {
                    int ix = 20 + (i % 3 * 35); 
                    int iy = 90 + (i / 3 * 35);
                    ItemStack stack = client.player.getInventory().getStack(i);
                    
                    context.fill(ix, iy, ix + 32, iy + 32, 0x66000000);
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
            if (System.currentTimeMillis() - startTime < 1200) return false;

            if (mouseX > 20 && mouseX < 140 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode;
                client.player.sendMessage(Text.literal("§e[FT] Сканер: " + (debugMode ? "§aВКЛ" : "§cВЫКЛ")), false);
                return true;
            }

            for (int i = 0; i < 9; i++) {
                int ix = 20 + (i % 3 * 35); 
                int iy = 90 + (i / 3 * 35);
                if (mouseX > ix && mouseX < ix + 32 && mouseY > iy && mouseY < iy + 32) {
                    ItemStack stack = client.player.getInventory().getStack(i);
                    if (!stack.isEmpty()) {
                        itemToSell = stack.getName().getString();
                        active = true;
                        client.player.sendMessage(Text.literal("§a[FT] Выбрано: §e" + itemToSell), false);
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
