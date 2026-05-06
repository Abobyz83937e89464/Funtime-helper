package com.example;

import com.example.util.render.DrawHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");

        // ✅ Правильная регистрация шейдера в 1.21.4
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(DrawHelper.ROUNDED_RECT);
        });
    }
}
