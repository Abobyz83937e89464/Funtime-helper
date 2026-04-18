package com.example;

import com.example.ui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NocturnClient implements ClientModInitializer {
    
    // Создаем бинд на Правый Shift
    private static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        // Регистрируем кнопку
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open ClickGUI", // Название в настройках
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, // Кнопка по умолчанию
                "Nocturn Client" // Категория в настройках
        ));

        // Проверяем нажатие каждый тик игры
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clickGuiKey.wasPressed()) {
                // Если мы в игре и меню не открыто — открываем наше ClickGUI
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGUI());
                }
            }
        });
    }
}
