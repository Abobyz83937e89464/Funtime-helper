package com.example;

import net.fabricmc.api.ClientModInitializer;

public class NocturnClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");
    }
}
