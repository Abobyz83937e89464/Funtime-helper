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
        // Команды на точку
        ClientSendMessageEvents.ALLOW_CHAT.register((msg) -> {
            if (msg.startsWith(".")) {
                processCommand(msg);
                return false;
            }
            return true;
        });

        // Слушаем чат сервера
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String text = message.getString();
            if (active && text.toLowerCase().contains("у вас купили")) {
                delayTicks = 40; // Ждем 2 секунды (40 тиков) перед перевыставлением
            }
        });

        // Цикл обработки задержки
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (delayTicks > 0) {
                delayTicks--;
                if (delayTicks == 0) {
                    executeAutoSell(client);
                }
            }
        });
    }

    private void processCommand(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (msg.equals(".")) {
            mc.player.sendMessage(Text.of("§6[Helper] Предметы в хотбаре:"), false);
            for (int i = 0; i < 9; i++) {
                ItemStack is = mc.player.getInventory().getStack(i);
                mc.player.sendMessage(Text.of("§e" + (i + 1) + ". " + is.getName().getString()), false);
            }
        } else if (msg.startsWith(".set ")) {
            try {
                int slot = Integer.parseInt(msg.split(" ")[1]) - 1;
                itemToSell = mc.player.getInventory().getStack(slot).getName().getString();
                active = true;
                mc.player.sendMessage(Text.of("§a[Helper] Мониторим: " + itemToSell), false);
            } catch (Exception e) {
                mc.player.sendMessage(Text.of("§cЮзай: .set [1-9]"), false);
            }
        } else if (msg.startsWith(".price ")) {
            price = Integer.parseInt(msg.split(" ")[1]);
            mc.player.sendMessage(Text.of("§a[Helper] Цена: " + price), false);
        }
    }

    private void executeAutoSell(MinecraftClient mc) {
        if (mc.player == null) return;
        
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getName().getString().equals(itemToSell)) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            // Если предмет в инвентаре, но не в руке — свопаем в 1 слот
            if (slot > 8) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
            }
            mc.getNetworkHandler().sendChatCommand("ah sell " + price);
        } else {
            active = false;
            mc.player.sendMessage(Text.of("§c[Helper] Закончился " + itemToSell), false);
        }
    }
}
