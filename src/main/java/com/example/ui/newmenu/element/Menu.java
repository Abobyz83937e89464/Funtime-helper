package com.example.ui.newmenu.element;

import com.example.util.render.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Menu extends Screen {

    private static final int W    = 400;
    private static final int H    = 240;
    private static final int SW   = 72;
    private static final int HDR  = 22;
    private static final int COLS = 2;
    private static final float PAD = 5f;

    private final CategoryElement catEl = new CategoryElement();
    private final ModuleElement   modEl = new ModuleElement();
    private final List<ModuleEntry> mods = new ArrayList<>();

    private float openAnim = 0f;
    private float scrollOff = 0f, scrollTgt = 0f, maxScroll = 0f;

    public Menu() {
        super(Text.of("Nocturn"));
        load();
    }

    private void load() {
        mods.clear();
        switch (catEl.getSelected()) {
            case COMBAT   -> add("KillAura","Velocity","AutoTotem","Criticals","Reach","AimAssist","AutoPot","AntiBot","Backtrack","HitBox","TriggerBot","AntiKnock");
            case MOVEMENT -> add("Speed","Fly","Sprint","NoSlow","Step","Strafe","LongJump","Blink");
            case RENDER   -> add("ESP","Tracers","NameTags","Fullbright","NoRender","ChestESP","HUD","CameraClip");
            case PLAYER   -> add("AutoArmor","ChestStealer","FastPlace","NoFall","Scaffold","AutoEat");
            case WORLD    -> add("Timer","Nuker","AutoMine","Fucker");
            case MISC     -> add("AutoFish","Disabler","Spammer","MiddleClick","AntiAFK");
        }
        scrollOff = 0; scrollTgt = 0;
        int rows = (int)Math.ceil((double)mods.size()/COLS);
        float ch = H - HDR;
        maxScroll = Math.max(0, rows*(ModuleElement.H+PAD)+PAD - ch);
    }

    private void add(String... names) {
        for (String n : names) mods.add(new ModuleEntry(n, false));
    }

    private float x()  { return (MinecraftClient.getInstance().getWindow().getScaledWidth()  - W) / 2f; }
    private float y()  { return (MinecraftClient.getInstance().getWindow().getScaledHeight() - H) / 2f; }
    private float cw() { return (W - SW - PAD*(COLS+1)) / COLS; }

    @Override
    public void tick() {
        openAnim  += (1f - openAnim)         * 0.14f;
        scrollOff += (scrollTgt - scrollOff) * 0.2f;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float x=x(), y=y();
        int   a=(int)(255*openAnim);

        // Оверлей
        DrawHelper.drawRect(ctx, 0, 0,
            MinecraftClient.getInstance().getWindow().getScaledWidth(),
            MinecraftClient.getInstance().getWindow().getScaledHeight(),
            new Color(0,0,0,(int)(85*openAnim)));

        // Анимация масштаба
        float sc=0.92f+openAnim*0.08f;
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x+W/2f,y+H/2f,0);
        ctx.getMatrices().scale(sc,sc,1);
        ctx.getMatrices().translate(-(x+W/2f),-(y+H/2f),0);

        // Основной фон
        DrawHelper.drawRoundedRect(ctx, x, y, W, H, 8, new Color(16,17,26,a));
        DrawHelper.drawOutline(ctx, x, y, W, H, 8, new Color(40,42,63,a));

        // Header
        DrawHelper.drawRoundedRect(ctx, x, y, W, HDR, 8, new Color(20,21,32,a));
        DrawHelper.drawRect(ctx, x, y+HDR-7, W, 7, new Color(20,21,32,a));
        DrawHelper.drawRect(ctx, x, y+HDR-1, W, 1, new Color(34,36,55,a));
        DrawHelper.drawGradientH(ctx, x, y, W*0.2f, HDR,
            new Color(46,50,98,(int)(42*openAnim)), new Color(16,17,26,0));

        // Лого — крупный
        float logoY = y+(HDR-DrawHelper.thBig())/2f;
        DrawHelper.textBig(ctx, "Nocturn", x+9, logoY, new Color(185,190,255,a));
        DrawHelper.textShadow(ctx, " Client", x+9+DrawHelper.twBig("Nocturn"), logoY+1, new Color(100,112,255,a));

        // Версия справа
        String ver="1.21.4";
        DrawHelper.text(ctx, ver, x+W-DrawHelper.tw(ver)-8, y+(HDR-DrawHelper.th())/2f, new Color(55,58,90,a));

        // Sidebar
        DrawHelper.drawRect(ctx, x, y+HDR, SW, H-HDR, new Color(12,13,20,a));
        DrawHelper.drawRect(ctx, x+SW, y+HDR, 1, H-HDR, new Color(34,36,55,a));
        catEl.render(ctx, x, y+HDR, SW, H-HDR);

        // Content
        float cx=x+SW+1, cy=y+HDR, cw=W-SW-1, ch=H-HDR;
        ctx.enableScissor((int)cx,(int)cy,(int)(cx+cw),(int)(cy+ch));

        float cardW=cw(), startY=cy+PAD+scrollOff;
        for (int i=0;i<mods.size();i++) {
            int col=i%COLS, row=i/COLS;
            float mx2=cx+PAD+col*(cardW+PAD);
            float my2=startY+row*(ModuleElement.H+PAD);
            if (my2+ModuleElement.H<cy||my2>cy+ch) continue;
            modEl.render(ctx, mx2, my2, cardW, mods.get(i), mouseX, mouseY);
        }

        ctx.disableScissor();

        // Fade
        DrawHelper.drawGradientV(ctx, cx, cy, cw, 10, new Color(16,17,26,a), new Color(16,17,26,0));
        DrawHelper.drawGradientV(ctx, cx, cy+ch-10, cw, 10, new Color(16,17,26,0), new Color(16,17,26,a));

        // Scrollbar
        if (maxScroll>0) {
            float tH=ch-8, tbH=Math.max(16,tH*ch/(ch+maxScroll));
            float prog=maxScroll>0?scrollOff/-maxScroll:0;
            float tbY=cy+4+prog*(tH-tbH);
            DrawHelper.drawRoundedRect(ctx, cx+cw-4, cy+4, 3, tH, 2, new Color(24,26,40,a));
            DrawHelper.drawRoundedRect(ctx, cx+cw-4, tbY, 3, tbH, 2, new Color(90,110,255,a));
        }

        ctx.getMatrices().pop();
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x=x(), y=y();
        if (mouseX>=x&&mouseX<=x+SW&&mouseY>=y+HDR&&mouseY<=y+H) {
            if (catEl.click(mouseX,mouseY)) { load(); return true; }
        }
        float cx=x+SW+1, cy=y+HDR, cardW=cw(), startY=cy+PAD+scrollOff;
        for (int i=0;i<mods.size();i++) {
            int col=i%COLS, row=i/COLS;
            float mx2=cx+PAD+col*(cardW+PAD), my2=startY+row*(ModuleElement.H+PAD);
            if (mouseX>=mx2&&mouseX<=mx2+cardW&&mouseY>=my2&&mouseY<=my2+ModuleElement.H)
                if (button==0) { mods.get(i).enabled=!mods.get(i).enabled; return true; }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        float x=x(), y=y();
        if (mouseX>=x+SW&&mouseX<=x+W&&mouseY>=y+HDR&&mouseY<=y+H) {
            scrollTgt=Math.max(-maxScroll,Math.min(0,scrollTgt+(float)(v*15)));
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
