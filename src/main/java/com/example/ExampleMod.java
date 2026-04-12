package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    // Переменная, чтобы перки не вылетали пачкой, пока держишь кнопку
    private boolean mouseWasDown = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null || client.currentScreen != null) return;

            // Проверяем нажатие СКМ (Middle Click) через GLFW
            boolean isMouseDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

            if (isMouseDown && !mouseWasDown) {
                throwPearl(client);
                mouseWasDown = true;
            } else if (!isMouseDown) {
                mouseWasDown = false;
            }
        });
    }

    private void throwPearl(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        int oldSlot = inventory.selectedSlot;
        int pearlSlot = -1;

        // Ищем эндер-перл в хотбаре (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).isOf(Items.ENDER_PEARL)) {
                pearlSlot = i;
                break;
            }
        }

        // Если нашли перку
        if (pearlSlot != -1) {
            // 1. Быстро переключаемся на слот с перкой
            inventory.selectedSlot = pearlSlot;
            
            // 2. Используем предмет (кидаем)
            // interactionManager делает это правильно, чтобы сервер засчитал бросок
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            
            // 3. Сразу возвращаем старый предмет в руки
            inventory.selectedSlot = oldSlot;
        }
    }
}
