package com.example;

import com.example.ui.newmenu.ClickGui; // Убедись, что путь к твоему классу меню верный
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NocturnClient implements ClientModInitializer {

    private static KeyBinding guiKeyBinding;

    @Override
    public void onInitializeClient() {
        // 1. Регистрируем кнопку (Right Shift по умолчанию)
        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nocturn.openscreen", 
                InputUtil.Type.KEYSYM, 
                GLFW.GLFW_KEY_RIGHT_SHIFT, // Тот самый Правый Шифт
                "category.nocturn.general"
        ));

        // 2. Слушаем каждое нажатие (тик) игры
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.wasPressed()) {
                // Если нажали кнопку — открываем наше меню
                client.setScreen(new ClickGui());
            }
        });

        System.out.println("Nocturn Client: Кнопка меню (RSHIFT) успешно зарегистрирована!");
    }
}
