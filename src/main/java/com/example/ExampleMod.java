package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.screen.slot.SlotActionType;

public class ExampleMod implements ClientModInitializer {
    private String itemToSell = "";
    private int price = 20000;
    private boolean active = false;
    private int delayTicks = -1;

    @Override
    public void onInitializeClient() {
        // Команды на точку (локальные)
        ClientSendMessageEvents.ALLOW_CHAT.register((msg) -> {
            if (msg.startsWith(".")) {
                processCommand(msg);
                return false; // Блокируем отправку точки на сервер
            }
            return true;
        });

        // Детектор чата с проверкой на любую позицию фразы
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String fullText = message.getString().toLowerCase(); // Весь текст в нижний регистр для точности
            
            // Проверяем, есть ли заветная фраза в сообщении
            if (active && fullText.contains("у вас купили")) {
                // Ставим задержку (40 тиков = ~2 сек), чтобы античит не ругался
                delayTicks = 40; 
            }
        });

        // Тик-менеджер для выполнения действий после задержки
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0 && active) {
                    executeAutoSell(client);
                }
            }
        });
    }

    private void processCommand(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (msg.equals(".")) {
            mc.player.sendMessage(Text.literal("§6[FT-Helper] Хотбар:"), false);
            for (int i = 0; i < 9; i++) {
                ItemStack is = mc.player.getInventory().getStack(i);
                mc.player.sendMessage(Text.literal("§e" + (i + 1) + ". " + is.getName().getString()), false);
            }
        } else if (msg.startsWith(".set ")) {
            try {
                int slot = Integer.parseInt(msg.split(" ")[1]) - 1;
                itemToSell = mc.player.getInventory().getStack(slot).getName().getString();
                active = true;
                mc.player.sendMessage(Text.literal("§a[FT-Helper] Цель: " + itemToSell), false);
            } catch (Exception e) {
                mc.player.sendMessage(Text.literal("§cОшибка! Используй: .set [номер]"), false);
            }
        } else if (msg.startsWith(".price ")) {
            try {
                price = Integer.parseInt(msg.split(" ")[1]);
                mc.player.sendMessage(Text.literal("§a[FT-Helper] Новая цена: " + price), false);
            } catch (Exception e) {
                mc.player.sendMessage(Text.literal("§cОшибка! Используй: .price [число]"), false);
            }
        }
    }

    private void executeAutoSell(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        int slot = -1;
        // Ищем предмет по всему инвентарю (0-35)
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getName().getString().equals(itemToSell)) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            // Если предмет в инвентаре, но не в первом слоте хотбара - свапаем его туда
            if (slot != 0) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
            }
            
            // Небольшая доп задержка после свапа может понадобиться, но попробуем так
            mc.getNetworkHandler().sendChatCommand("ah sell " + price);
            mc.player.sendMessage(Text.literal("§b[FT-Helper] Авто-выставление: " + itemToSell), false);
        } else {
            active = false;
            mc.player.sendMessage(Text.literal("§c[FT-Helper] Предметы закончились! Отдыхаем."), false);
        }
    }
}
