package net.cyberflame.viewmodel.gui;

import net.cyberflame.viewmodel.config.SaveConfig;
import net.cyberflame.viewmodel.settings.FloatSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider implements ViewmodelGuiObj {

    private final FloatSetting setting;
    private final int x, y, width, height;
    private final float min, max;
    private boolean isDragging = false;
    private boolean isHovered = false;
    private float hoverAnimation = 0.0f;

    public Slider(@NotNull FloatSetting setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y + 30; // Смещаем вниз для текста сверху
        this.width = width;
        this.height = 16; // Фиксированная высота слайдера
        this.min = setting.getMin();
        this.max = setting.getMax();
    }

    private static float round(float value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @Override
    public final void mouseScrolled(double mx, double my, float incx, float incy) {
        float delta = Math.abs(incx) >= Math.abs(incy) ? incx : incy;
        if (delta == 0f) return;

        float increment = (this.max - this.min) * 0.01f;
        float newValue = this.setting.getValue() + delta * increment;
        this.setting.setValue(MathHelper.clamp(newValue, this.min, this.max));

        SaveConfig.saveAllSettings();
    }

    @Override
    public final void mouseClicked(double mx, double my) {
        if (isWithin(mx, my)) {
            isDragging = true;
            updateValue(mx);
        }
    }

    @Override
    public void mouseReleased(double mx, double my) {
        isDragging = false;
    }

    @Override
    public void mouseDragged(double mx, double my, double deltaX, double deltaY) {
        if (isDragging) {
            updateValue(mx);
        }
    }

    private void updateValue(double mx) {
        float ratio = (float) MathHelper.clamp((mx - this.x) / this.width, 0.0, 1.0);
        float newValue = this.min + ratio * (this.max - this.min);
        this.setting.setValue(MathHelper.clamp(newValue, this.min, this.max));

        SaveConfig.saveAllSettings();
    }

    @Override
    public final void render(@NotNull DrawContext context, int mouseX, int mouseY) {
        boolean wasHovered = isHovered;
        isHovered = isWithin(mouseX, mouseY);

        if (isHovered || isDragging) {
            hoverAnimation = Math.min(1.0f, hoverAnimation + 0.15f);
        } else {
            hoverAnimation = Math.max(0.0f, hoverAnimation - 0.15f);
        }

        String settingName = this.setting.getName();
        float settingValue = this.setting.getValue();

        // Текст сверху слева
        int nameColor = isHovered ? 0xFFFFFFFF : 0xFFCCCCCC;
        context.drawTextWithShadow(
                ViewmodelScreen.mc.textRenderer,
                settingName,
                this.x,
                this.y - 15,
                nameColor
        );

        // Значение сверху справа
        String valueStr = String.valueOf(round(settingValue));
        int valueWidth = ViewmodelScreen.mc.textRenderer.getWidth(valueStr);
        int valueColor = isDragging ? 0xFF66B2FF : (isHovered ? 0xFFFFFFFF : 0xFFCCCCCC);
        context.drawTextWithShadow(
                ViewmodelScreen.mc.textRenderer,
                valueStr,
                this.x + this.width - valueWidth + 32,
                this.y + 4,
                valueColor
        );

        int cardAlpha = (int) (0x35 + hoverAnimation * 0x15);
        int bgColor = (cardAlpha << 24) | 0xFFFFFF;

        context.fill(this.x + 1, this.y + 1, this.x + this.width + 1, this.y + this.height + 1, 0x40000000);
        context.fill(this.x, this.y, this.x + this.width, this.y + this.height, bgColor);

        float ratio = (settingValue - this.min) / (this.max - this.min);
        int filledWidth = (int) (this.width * ratio);

        if (filledWidth > 0) {
            int progressColor;
            if (isDragging) {
                progressColor = 0xFF66B2FF;
            } else if (isHovered) {
                progressColor = 0xFF4A9EE0;
            } else {
                progressColor = 0xFF3A8ED0;
            }

            context.fill(this.x, this.y, this.x + filledWidth, this.y + this.height, progressColor);
        }

        int borderColor;
        if (isDragging) {
            borderColor = 0xFF66B2FF;
        } else if (isHovered) {
            borderColor = 0x80FFFFFF;
        } else {
            borderColor = 0x40FFFFFF;
        }
        drawBorder(context, this.x, this.y, this.width, this.height, borderColor);

        int handleX = this.x + filledWidth;
        int baseHandleSize = this.height + 4;
        int handleSize = baseHandleSize + (int) (hoverAnimation * 3);
        int handleY = this.y - 2 - (int) (hoverAnimation * 1.5f);

        int handleHalfSize = handleSize / 2;
        handleX = Math.max(this.x + handleHalfSize, Math.min(this.x + this.width - handleHalfSize, handleX));

        drawRoundedHandle(context, handleX - handleHalfSize, handleY, handleSize, handleSize,
                isDragging ? 0xFFFFFFFF : 0xFFF0F0F0);
    }

    private void drawRoundedHandle(DrawContext context, int x, int y, int width, int height, int color) {
        drawCircle(context, x + 1, y + 1, width, height, 0x60000000);
        drawCircle(context, x, y, width, height, color);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private void drawCircle(DrawContext context, int x, int y, int width, int height, int color) {
        int rx = width / 2;
        int ry = height / 2;
        int cx = x + rx;
        int cy = y + ry;

        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + 2, y + height - 2, color);
        context.fill(x + width - 2, y + 2, x + width, y + height - 2, color);

        context.fill(x + 1, y + 1, x + 3, y + 3, color);
        context.fill(x + width - 3, y + 1, x + width - 1, y + 3, color);
        context.fill(x + 1, y + height - 3, x + 3, y + height - 1, color);
        context.fill(x + width - 3, y + height - 3, x + width - 1, y + height - 1, color);
    }

    @Contract(pure = true)
    @Override
    public final boolean isWithin(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y &&
                mouseX <= this.x + this.width && mouseY <= this.y + this.height;
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }
}