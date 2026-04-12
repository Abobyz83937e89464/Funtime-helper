package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private boolean isDown = false;

    @Override
    public void onInitializeClient() {
        // Используем более легкий метод проверки
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;

            // Проверка через GLFW только если окно активно
            boolean pressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

            if (pressed && !isDown) {
                isDown = true;
                // Запускаем через планировщик клиента, чтобы не фризить основной поток
                client.execute(() -> {
                    int oldSlot = client.player.getInventory().selectedSlot;
                    int pearlSlot = findPearl(client);
                    if (pearlSlot != -1) {
                        client.player.getInventory().selectedSlot = pearlSlot;
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        client.player.getInventory().selectedSlot = oldSlot;
                    }
                });
            } else if (!pressed) {
                isDown = false;
            }
        });
    }

    private int findPearl(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) return i;
        }
        return -1;
    }
}
