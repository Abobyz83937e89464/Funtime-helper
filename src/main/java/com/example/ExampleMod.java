package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding pearlKey;

    @Override
    public void onInitializeClient() {
        // Для мыши теперь нужно обязательно использовать InputUtil.Type.MOUSE
        pearlKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Быстрая перка", 
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 
                "FT Helper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.currentScreen != null) return;

            while (pearlKey.wasPressed()) {
                throwPearl(client);
            }
        });
    }

    private void throwPearl(MinecraftClient client) {
        int oldSlot = client.player.getInventory().selectedSlot;
        int pearlSlot = -1;

        // Ищем эндер-перл в хотбаре (слоты от 0 до 8)
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
                pearlSlot = i;
                break;
            }
        }

        if (pearlSlot != -1) {
            // 1. Отправляем на сервер пакет о том, что мы выбрали слот с перлом (чтобы не было десинка)
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
            client.player.getInventory().selectedSlot = pearlSlot;

            // 2. Имитируем нажатие ПКМ (бросок)
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

            // 3. Возвращаем старый слот и сообщаем об этом серверу
            client.player.getInventory().selectedSlot = oldSlot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
        }
    }
}
