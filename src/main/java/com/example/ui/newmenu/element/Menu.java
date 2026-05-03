package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import com.example.util.render.Fonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu extends Screen {

    // Размеры как в оригинале, но scaled down
    private static final int CONTENT_W = 460;
    private static final int CONTENT_H = 240;
    private static final int HEADER_H  = 36;
    private static final int FOOTER_H  = 38;
    private static final int TOTAL_H   = HEADER_H + CONTENT_H + FOOTER_H;

    private static final int   COLS      = 4;
    private static final float MOD_W     = ModuleElement.getModuleWidth();
    private static final int   PADDING   = 8;

    private final CategoryElement categoryElement = new CategoryElement();
    private final ModuleElement   moduleElement   = new ModuleElement();
    private final List<ModuleEntry> modules = new ArrayList<>();

    private float scrollValue  = 0f;
    private float scrollTarget = 0f;
    private float maxScroll    = 0f;
    private float openAnim     = 0f;

    public Menu() {
        super(Text.of("Menu"));
        updateCurrentModules();
    }

    private void updateCurrentModules() {
        modules.clear();
        switch (categoryElement.getSelectedCategory()) {
            case COMBAT   -> addMods("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> addMods("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> addMods("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> addMods("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> addMods("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> addMods("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollValue = 0; scrollTarget = 0;
        updateMaxScroll();
    }

    private void addMods(String... names) {
        for (String n : names) modules.add(new ModuleEntry(n, false));
    }

    private void updateMaxScroll() {
        int[] colH = new int[COLS];
        for (int i = 0; i < modules.size(); i++) {
            colH[i%COLS] += (int)(moduleElement.getHeight() + PADDING);
        }
        int maxColH = 0;
        for (int h : colH) maxColH = Math.max(maxColH, h);
        maxScroll = Math.max(0, maxColH - CONTENT_H);
    }

    // ── Позиции ───────────────────────────────────────────────
    private float screenX() { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - CONTENT_W) / 2f; }
    private float screenY() { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - TOTAL_H)   / 2f; }

    @Override
    public void tick() {
        super.tick();
        openAnim    += (1f - openAnim)          * 0.14f;
        scrollValue += (scrollTarget - scrollValue) * 0.2f;
        updateMaxScroll();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MatrixStack ms = ctx.getMatrices();
        float x  = screenX();
        float y  = screenY();
        float cx = x; // content X
        float cy = y + HEADER_H; // content Y
        int   a  = (int)(255 * openAnim);

        // Тёмный оверлей
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0,0,0,(int)(110*openAnim)));

        // Анимация scale
        float sc = 0.93f + openAnim*0.07f;
        ms.push();
        ms.translate(x+CONTENT_W/2f, y+TOTAL_H/2f, 0);
        ms.scale(sc,sc,1);
        ms.translate(-(x+CONTENT_W/2f), -(y+TOTAL_H/2f), 0);

        // ── HEADER ──────────────────────────────────────────
        // Скруглённые только верхние углы
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W, HEADER_H,
            20, 0, 0, 20,
            new Color(29,31,44,(int)(204*openAnim)));
        DrawHelper.drawGradientH(ms.peek().getPositionMatrix(),
            x, y, CONTENT_W*0.25f, HEADER_H,
            new Color(51,56,94,(int)(60*openAnim)), new Color(29,31,44,0));

        // Лого
        String logo = "Nocturn";
        String sub  = " Client";
        float logoY = y + (HEADER_H - Fonts.height())/2f;
        DrawHelper.drawTextShadow(ctx, logo, x+14, logoY, new Color(197,200,255,a));
        DrawHelper.drawText(ctx, sub, x+14+Fonts.width(logo), logoY, new Color(125,136,255,a));

        // Категория по центру
        String catName = categoryElement.getSelectedCategory().getDisplayName();
        DrawHelper.drawText(ctx, catName,
            x+(CONTENT_W-Fonts.width(catName))/2f, logoY,
            new Color(141,144,199,a));

        // Версия справа
        String ver = "v1.0 | 1.21.4";
        DrawHelper.drawText(ctx, ver,
            x+CONTENT_W-Fonts.width(ver)-14, logoY,
            new Color(80,84,120,a));

        // ── CONTENT ─────────────────────────────────────────
        DrawHelper.drawRect(ctx, cx, cy, CONTENT_W, CONTENT_H, new Color(17,19,24,a));

        // Общий outline всего меню
        DrawHelper.drawOutline(ms, x-2, y-2, CONTENT_W+4, TOTAL_H+4,
            20, new Color(52,51,64,a), 3);

        // Scissor для контента
        ctx.enableScissor((int)cx,(int)cy,(int)(cx+CONTENT_W),(int)(cy+CONTENT_H));
        renderModules(ctx, (int)cx+PADDING, (int)cy+PADDING, mouseX, mouseY);
        ctx.disableScissor();

        // Fade сверху/снизу
        DrawHelper.drawGradientV(ctx, cx, cy, CONTENT_W, 14,
            new Color(17,19,24,a), new Color(17,19,24,0));
        DrawHelper.drawGradientV(ctx, cx, cy+CONTENT_H-14, CONTENT_W, 14,
            new Color(17,19,24,0), new Color(17,19,24,a));

        // Scrollbar
        if (maxScroll > 0) {
            float trkH = CONTENT_H-8;
            float tmbH = Math.max(20, trkH*CONTENT_H/(CONTENT_H+maxScroll));
            float prog  = maxScroll>0 ? scrollValue/maxScroll : 0f;
            float tmbY  = cy+4+prog*(trkH-tmbH);
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), cx+CONTENT_W-4, cy+4, 3, trkH, 2, new Color(28,30,42,a));
            DrawHelper.drawRect(ms.peek().getPositionMatrix(), cx+CONTENT_W-4, tmbY, 3, tmbH, 2, new Color(125,136,255,a));
        }

        // ── FOOTER ──────────────────────────────────────────
        float fy = cy + CONTENT_H;
        // Скруглённые только нижние углы
        DrawHelper.drawStyledRect(ms.peek().getPositionMatrix(),
            x, fy, CONTENT_W, FOOTER_H,
            0, 20, 20, 0,
            new Color(29,31,44,(int)(204*openAnim)));
        DrawHelper.drawGradientH(ms.peek().getPositionMatrix(),
            x, fy, CONTENT_W*0.25f, FOOTER_H,
            new Color(51,56,94,(int)(40*openAnim)), new Color(29,31,44,0));

        // Категории
        categoryElement.renderFooter(ctx, x, fy, CONTENT_W, FOOTER_H);

        ms.pop();
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderModules(DrawContext ctx, int startX, int startY, int mouseX, int mouseY) {
        int[] colX = new int[COLS];
        for (int i=0;i<COLS;i++) colX[i] = startX + i*(int)(MOD_W+PADDING);

        int[] colY = new int[COLS];
        Arrays.fill(colY, (int)(startY - scrollValue));

        for (int i=0; i<modules.size(); i++) {
            int col = i%COLS;
            float mx2=colX[col], my2=colY[col];
            float mh=moduleElement.getHeight();
            // Видимость
            float cy=screenY()+HEADER_H;
            if (my2+mh >= cy && my2 <= cy+CONTENT_H) {
                moduleElement.render(ctx, mx2, my2, modules.get(i), mouseX, mouseY);
            }
            colY[col] += (int)(mh + PADDING);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x=screenX(), y=screenY();
        float fy=y+HEADER_H+CONTENT_H;

        // Footer — категории
        if (mouseY>=fy && mouseY<=fy+FOOTER_H) {
            if (categoryElement.mouseClicked(mouseX, mouseY)) {
                updateCurrentModules(); return true;
            }
        }

        // Контент — модули
        int[] colX=new int[COLS];
        for(int i=0;i<COLS;i++) colX[i]=(int)(x+PADDING)+i*(int)(MOD_W+PADDING);
        int[] colY=new int[COLS];
        Arrays.fill(colY,(int)(y+HEADER_H+PADDING-(int)scrollValue));

        for (int i=0;i<modules.size();i++) {
            int col=i%COLS;
            if (moduleElement.mouseClicked(colX[col],colY[col],modules.get(i),mouseX,mouseY,button)) return true;
            colY[col]+=(int)(moduleElement.getHeight()+PADDING);
        }

        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x=screenX(), y=screenY();
        if (mouseX>=x&&mouseX<=x+CONTENT_W&&mouseY>=y+HEADER_H&&mouseY<=y+HEADER_H+CONTENT_H) {
            scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget-(float)(v*18)));
            return true;
        }
        return super.mouseScrolled(mouseX,mouseY,h,v);
    }

    @Override
    public boolean keyPressed(int key,int scan,int mods){
        if(key==256){close();return true;}
        return super.keyPressed(key,scan,mods);
    }

    @Override public boolean shouldPause(){return false;}

    public static class ModuleEntry {
        public String name; public boolean enabled;
        public ModuleEntry(String n,boolean e){name=n;enabled=e;}
    }
}
