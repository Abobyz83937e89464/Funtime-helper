package com.example;

import com.example.util.render.DrawHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");

        // В 1.21.4 шейдеры регистрируются ТОЛЬКО через CoreShaderRegistrationCallback.
        // ResourceReloadListener + new ShaderProgram(...) — старый API, сломан.
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            try {
                context.register(
                    Identifier.of("nocturn-client", "rounded_rect"),
                    VertexFormats.POSITION_TEXTURE,          // должно совпадать с "attributes" в .json
                    program -> DrawHelper.shader = program   // колбэк сохраняет ссылку
                );
            } catch (Exception e) {
                System.err.println("[Nocturn] Shader registration failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
