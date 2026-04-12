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
    public static boolean debugMode = false; // Тот самый сканер
    private int delayTicks = -1;

    @Override
    public void onInitializeClient() {
        // 1. Регистрация кнопки G
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Открыть Хелпер", GLFW.GLFW_KEY_G, "FunTime Helper"
        ));

        // 2. Открытие меню по нажатию
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.wasPressed()) {
                client.setScreen(new HelperScreen());
            }

            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && active) executeAutoSell(client);
            }
        });

        // 3. Сканер чата
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String fullText = message.getString();
            
            // Если включен дебаг - пишем в чат всё что видим в ковычках
            if (debugMode) {
                MinecraftClient.getInstance().player.sendMessage(
                    Text.literal("§7[DEBUG] Пришло: §f\"" + fullText + "\""), false
                );
            }

            if (active && fullText.toLowerCase().contains("у вас купили")) {
                delayTicks = 30; 
            }
        });
    }

    private void executeAutoSell(MinecraftClient mc) {
        if (mc.player == null) return;
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

    // --- ВНУТРЕННИЙ КЛАСС МЕНЮ ---
    public static class HelperScreen extends Screen {
        private float animProgress = 0; // Для анимации вылета

        protected HelperScreen() {
            super(Text.literal("Helper Menu"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Анимация: двигаем меню слева направо
            if (animProgress < 1.0f) animProgress += delta * 0.1f;
            int xOffset = (int) ((1.0f - animProgress) * -200);

            // Фон меню
            context.fill(xOffset + 10, 10, xOffset + 160, height - 10, 0xAA000000);
            context.drawText(context.textRenderer, "§6§lFT HELPER", xOffset + 20, 20, 0xFFFFFF, true);

            // Кнопка Сканера
            int debugColor = debugMode ? 0xFF55FF55 : 0xFFFF5555;
            context.fill(xOffset + 20, 40, xOffset + 150, 60, 0x44FFFFFF);
            context.drawText(context.textRenderer, "Сканер: " + (debugMode ? "ВКЛ" : "ВЫКЛ"), xOffset + 30, 45, debugColor, false);

            // Отрисовка инвентаря (хотбара) для выбора
            context.drawText(context.textRenderer, "Выбери предмет:", xOffset + 20, 75, 0xAAAAAA, false);
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                int x = xOffset + 25 + (col * 35);
                int y = 90 + (row * 35);
                
                ItemStack stack = client.player.getInventory().getStack(i);
                context.fill(x, y, x + 32, y + 32, 0x33FFFFFF);
                context.drawItem(stack, x + 8, y + 8);
                
                // Если этот предмет выбран - подсвечиваем
                if (stack.getName().getString().equals(itemToSell)) {
                    context.drawBorder(x, y, 32, 32, 0xFF55FF55);
                }
            }
            super.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Логика нажатия на кнопку сканера
            if (mouseX > 20 && mouseX < 150 && mouseY > 40 && mouseY < 60) {
                debugMode = !debugMode;
                client.player.sendMessage(Text.literal("§eСканер: " + (debugMode ? "§aВКЛ" : "§cВЫКЛ")), false);
                return true;
            }

            // Логика выбора предмета из сетки
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                int x = 25 + (col * 35);
                int y = 90 + (row * 35);
                if (mouseX > x && mouseX < x + 32 && mouseY > y && mouseY < y + 32) {
                    itemToSell = client.player.getInventory().getStack(i).getName().getString();
                    active = true;
                    client.player.sendMessage(Text.literal("§a[Helper] Выбрано: " + itemToSell), false);
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
