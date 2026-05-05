package com.example;

import com.example.util.render.DrawHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DrawHelper.initShader();
        });
    }
}
