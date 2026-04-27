package com.example;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NocturnClient implements ClientModInitializer {
    
    public static final String MOD_ID = "nocturn-client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // Этот код выполнится при запуске Майнкрафта
        LOGGER.info("Nocturn Client успешно запущен!");
        
        // Позже здесь мы добавим регистрацию кнопки (например, Right Shift), 
        // чтобы по ней открывалось твоё меню.
    }
}
