package com.example.ui.newmenu;

import com.example.NocturnClient;
import com.example.ui.newmenu.element.CategoryElement;
import com.example.ui.newmenu.element.ModuleElement;
// Импорты ниже пока будут гореть красным, мы их добавим позже
// import com.example.util.other.ScrollHandler;
// import com.example.util.render.DrawHelper;
// import com.example.util.render.ScaleUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Menu extends Screen {

    // Временно закомментировали то, чего еще нет, чтобы это была рабочая "пустышка"
    // private final InfoElement infoElement;
    private final ModuleElement moduleElement;
    private final CategoryElement categoryElement;
    
    // Заглушки для модулей, пока не подключим твой ModuleManager
    // private final Map<Category, List<Module>> modulesByCategory;
    // private List<Module> currentModules;
    // private final ScrollHandler scrollHandler = new ScrollHandler();

    public Menu() {
        super(Text.of("Nocturn Menu"));
        // this.infoElement = new InfoElement();
        this.moduleElement = new ModuleElement();
        this.categoryElement = new CategoryElement();

        // Позже здесь будет загрузка модулей из NocturnClient
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int x = (screenWidth - 800) / 2; // Временно уменьшил размер для пустышки
        int y = (screenHeight - 500) / 2;

        // ВРЕМЕННЫЙ РЕНДЕР ПУСТЫШКИ (Пока нет DrawHelper)
        // Заливаем фон меню дефолтными методами майнкрафта
        context.fill(x, y, x + 800, y + 500, new Color(17, 19, 24, 255).getRGB());
        
        // Отрисовка категорий снизу
        categoryElement.renderFooter(context, x, y + 500, 80);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
