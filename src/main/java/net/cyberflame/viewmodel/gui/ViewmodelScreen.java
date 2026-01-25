package net.cyberflame.viewmodel.gui;

import net.cyberflame.viewmodel.Viewmodel;
import net.cyberflame.viewmodel.settings.BooleanSetting;
import net.cyberflame.viewmodel.settings.Setting;
import net.cyberflame.viewmodel.settings.SettingType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ViewmodelScreen extends Screen {

    static final MinecraftClient mc = MinecraftClient.getInstance();

    private final Collection<ViewmodelGuiObj> mainHandObjs = new ArrayList<>();
    private final Collection<ViewmodelGuiObj> offHandObjs = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int scaledPadding;
    private int scaledSpacing;
    private int columnWidth;
    private int centerGap;

    public ViewmodelScreen() {
        super(Text.translatable("viewmodel.screen.title"));
    }

    @Override
    public void init() {
        mainHandObjs.clear();
        offHandObjs.clear();

        calculateScaledSizes();

        Arm mainArm = mc.options.getMainArm().getValue();
        boolean rightHanded = mainArm == Arm.RIGHT;

        int center = width / 2;
        int leftX = center - columnWidth - centerGap / 2;
        int rightX = center + centerGap / 2;

        int baseY = scaledPadding + 50;

        createColumn(mainHandObjs, leftX, baseY, true, rightHanded);
        createColumn(offHandObjs, rightX, baseY, false, !rightHanded);
    }

    private void calculateScaledSizes() {
        float scale = (float) client.getWindow().getScaleFactor();

        this.scaledPadding = height / 12;
        this.scaledSpacing = 35; // Было 20-25, увеличиваем для места под текст сверху
        this.columnWidth = width / 3;
        this.centerGap = width / 15;
    }

    private void createColumn(Collection<ViewmodelGuiObj> objs, int x, int startY, boolean isMain, boolean isActiveHand) {
        int y = startY;

        for (SettingType type : SettingType.values()) {
            boolean belongsToMain = type.name().startsWith("MAIN_");
            if (belongsToMain != isMain) continue;

            Setting<?> setting = type.getSetting();

            // Показываем все настройки второй руки даже если OFF_HAND_ENABLED выключен
            // (можно добавить условие continue, если хочешь скрывать)

            String label = Text.translatable(type.getLangKey()).getString();
            String tooltip = Text.translatable(type.getLangKey() + ".tooltip").getString();

            int h = scaledSpacing;

            setting.createUIElementWithTooltip(objs, 0, x + 8, y + scrollOffset, columnWidth - 16, h, tooltip);

            y += scaledSpacing + (setting instanceof BooleanSetting ? 10 : 4);
        }

        maxScroll = Math.max(maxScroll, y - height + scaledPadding * 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);

        Arm mainArm = mc.options.getMainArm().getValue();
        boolean rightHanded = mainArm == Arm.RIGHT;

        String leftTitle  = Text.translatable(rightHanded ? "viewmodel.hand.right" : "viewmodel.hand.left").getString();
        String leftSub    = Text.translatable("").getString();

        String rightTitle = Text.translatable(rightHanded ? "viewmodel.hand.left"  : "viewmodel.hand.right").getString();
        String rightSub   = Text.translatable("").getString();

        int cardH = height - scaledPadding * 2 - 40;

        drawCard(context, width/2 - columnWidth - centerGap/2, scaledPadding + 20, columnWidth, cardH, leftTitle,  leftSub,  0xFF4A90E2);
        drawCard(context, width/2 + centerGap/2,               scaledPadding + 20, columnWidth, cardH, rightTitle, rightSub, 0xFFE94E77);

        mainHandObjs.forEach(o -> o.render(context, mouseX, mouseY));
        offHandObjs.forEach(o -> o.render(context, mouseX, mouseY));

        drawBottomHint(context);
    }

    private void drawCard(DrawContext ctx, int x, int y, int w, int h, String title, String sub, int accent) {
        ctx.fill(x - 2, y - 2, x + w + 4, (y + h) - 80 , 0x50000000);
        ctx.fill(x, y, x + w, y + 3, accent);

        int tw = textRenderer.getWidth(title);
        ctx.drawCenteredTextWithShadow(textRenderer, title, x + w/2, y + 8, 0xFFFFFFFF);

        int sw = textRenderer.getWidth(sub);
        ctx.drawCenteredTextWithShadow(textRenderer, sub, x + w/2, y + 22, 0xFFAAAAAA);
    }

    private void drawBottomHint(DrawContext ctx) {
        String hint = Text.translatable("viewmodel.hint.controls").getString();
        int hw = textRenderer.getWidth(hint);
        int hx = (width - hw) / 2;
        int hy = height - 24;

        ctx.fill(hx - 16, hy - 8, hx + hw + 16, hy + 16, 0x40000000);
        ctx.drawCenteredTextWithShadow(textRenderer, hint, hx + hw/2, hy, 0xFFDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (ViewmodelGuiObj o : mainHandObjs) if (o.isWithin(mx, my)) { o.mouseClicked(mx, my); return true; }
        for (ViewmodelGuiObj o : offHandObjs) if (o.isWithin(mx, my)) { o.mouseClicked(mx, my); return true; }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        mainHandObjs.forEach(o -> o.mouseReleased(mx, my));
        offHandObjs.forEach(o -> o.mouseReleased(mx, my));
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        for (ViewmodelGuiObj o : mainHandObjs)  if (o.isWithin(mx, my) || o.isDragging()) { o.mouseDragged(mx, my, dx, dy); return true; }
        for (ViewmodelGuiObj o : offHandObjs) if (o.isWithin(mx, my) || o.isDragging()) { o.mouseDragged(mx, my, dx, dy); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amountX, double amountY) {
        scrollOffset += (int) (amountY * scaledSpacing * 1.5);
        scrollOffset = Math.clamp(scrollOffset, -maxScroll, 0);
        init();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}