package com.example;

import com.example.util.render.Fonts;
import net.fabricmc.api.ClientModInitializer;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Fonts.init();
        System.out.println("[Nocturn Client] Loaded!");
    }
}
