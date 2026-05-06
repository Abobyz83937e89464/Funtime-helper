package com.example;

import com.example.util.render.DrawHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class NocturnClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Nocturn Client] Loaded!");

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {

                @Override
                public Identifier getFabricId() {
                    return Identifier.of("nocturn-client", "drawhelper");
                }

                @Override
                public void reload(ResourceManager manager) {
                    // ✅ ResourceManager, не ResourceFactory
                    DrawHelper.init(manager);
                }
            });
    }
}
