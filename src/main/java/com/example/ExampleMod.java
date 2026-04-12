package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding pearlKey;

    @Override
    public void onInitializeClient() {
        // Регистрируем кнопку. GLFW_MOUSE_BUTTON_MIDDLE — это и есть СКМ.
        pearlKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Быстрая перка", 
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 
                "FT Helper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;

            // Используем стандартный метод wasPressed() — он самый легкий для ЦП
            while (pearlKey.wasPressed()) {
                throwPearl(client);
            }
        });
    }

    private void throwPearl(MinecraftClient client) {
        // Выполняем бросок в безопасном потоке
        client.execute(() -> {
            int oldSlot = client.player.getInventory().selectedSlot;
            int pearlSlot = -1;

            for (int i = 0; i < 9; i++) {
                if (client.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
                    pearlSlot = i;
                    break;
                }
            }

            if (pearlSlot != -1) {
                // Моментальный свап и бросок
                client.player.getInventory().selectedSlot = pearlSlot;
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                client.player.getInventory().selectedSlot = oldSlot;
            }
        });
    }
}
