package com.example;

import com.example.util.render.DrawHelper;
import net.fabricmc.api.ClientModInitializer;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");

        // Регистрируем шейдер через CoreShaderRegistrationCallback
        // (должно быть ДО первого рендер-фрейма)
        DrawHelper.registerShaders();
    }
}
