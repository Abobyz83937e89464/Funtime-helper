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
        ClientSendMessageEvents.ALLOW_CHAT.register((msg) -> {
            if (msg.startsWith(".")) {
                processCommand(msg);
                return false;
            }
            return true;
        });

        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, time) -> {
            String text = message.getString().toLowerCase();
            if (active && text.contains("у вас купили")) {
                delayTicks = 40; 
            }
        });

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
            mc.player.sendMessage(Text.literal("§6[Helper] Предметы в хотбаре:"), false);
            for (int i = 0; i < 9; i++) {
                ItemStack is = mc.player.getInventory().getStack(i);
                mc.player.sendMessage(Text.literal("§e" + (i + 1) + ". " + is.getName().getString()), false);
            }
        } else if (msg.startsWith(".set ")) {
            try {
                int slot = Integer.parseInt(msg.split(" ")[1]) - 1;
                itemToSell = mc.player.getInventory().getStack(slot).getName().getString();
                active = true;
                mc.player.sendMessage(Text.literal("§a[Helper] Мониторим: " + itemToSell), false);
            } catch (Exception e) {
                mc.player.sendMessage(Text.literal("§cЮзай: .set [1-9]"), false);
            }
        }
    }

    private void executeAutoSell(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getName().getString().equals(itemToSell)) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            if (slot > 8) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.SWAP, mc.player);
            }
            mc.getNetworkHandler().sendChatCommand("ah sell " + price);
            mc.player.sendMessage(Text.literal("§b[Helper] Выставил предмет!"), false);
        } else {
            active = false;
            mc.player.sendMessage(Text.literal("§c[Helper] Предмет закончился."), false);
        }
    }
}
