package com.example;

import com.example.ui.newmenu.Menu; // Теперь импортируем новое меню
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NocturnClient implements ModInitializer {

    private static KeyBinding guiKeyBind;

    @Override
    public void onInitialize() {
        // Регистрируем кнопку открытия меню (Правый Шифт)
        guiKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "Nocturn Client"
        ));

        // Слушаем нажатия в каждом тике клиента
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBind.wasPressed()) {
                // Вызываем наше новое меню
                client.setScreen(new Menu());
            }
        });
    }
}
