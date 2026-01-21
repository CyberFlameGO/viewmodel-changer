package net.cyberflame.viewmodel.gui;

import net.cyberflame.viewmodel.settings.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Современный переключатель в стиле iOS с эффектом Liquid Glass.
 */
public class Switch implements ViewmodelGuiObj {

    private final BooleanSetting setting;
    private final int x, y;
    private final int width, height;
    private boolean isHovered = false;
    private float animationProgress = 0.0f;
    private float hoverAnimation = 0.0f;
    private String tooltip = "";

    public Switch(@NotNull BooleanSetting setting, int x, int y, int size) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = size * 3;
        this.height = size;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public final void mouseClicked(double mx, double my) {
        if (isWithin(mx, my)) {
            this.setting.setValue(!this.setting.getValue());
        }
    }

    @Override
    public void mouseReleased(double mx, double my) {
        // Не требуется
    }

    @Override
    public void mouseDragged(double mx, double my, double deltaX, double deltaY) {
        // Не требуется
    }

    @Override
    public final void mouseScrolled(double mx, double my, float incx, float incy) {
        // Не используется
    }

    @Override
    public final void render(@NotNull DrawContext context, int mouseX, int mouseY) {
        isHovered = isWithin(mouseX, mouseY);
        boolean isEnabled = this.setting.getValue();

        // Плавная анимация переключения
        float targetProgress = isEnabled ? 1.0f : 0.0f;
        float animSpeed = 0.25f;
        animationProgress += (targetProgress - animationProgress) * animSpeed;

        // Анимация наведения
        if (isHovered) {
            hoverAnimation = Math.min(1.0f, hoverAnimation + 0.15f);
        } else {
            hoverAnimation = Math.max(0.0f, hoverAnimation - 0.15f);
        }

        String settingName = this.setting.getName();

        // Название слева
        int nameColor = isHovered ? 0xFFFFFFFF : 0xFFCCCCCC;
        context.drawTextWithShadow(
                ViewmodelScreen.mc.textRenderer,
                settingName,
                this.x - ViewmodelScreen.mc.textRenderer.getWidth(settingName) - 10,
                this.y + this.height / 2 - ViewmodelScreen.mc.textRenderer.fontHeight / 2,
                nameColor
        );

        // Тень переключателя
        context.fill(this.x + 1, this.y + 1, this.x + this.width + 1, this.y + this.height + 1, 0x50000000);

        // Фон переключателя с градиентом
        int bgColor;
        if (isEnabled) {
            // Зеленый градиент для включенного состояния
            int greenBase = 0x34C759; // iOS зеленый
            int alpha = 0xFF;
            bgColor = (alpha << 24) | greenBase;
        } else {
            // Серый для выключенного состояния
            bgColor = isHovered ? 0xFF555555 : 0xFF3A3A3A;
        }

        // Основной фон
        drawRoundedRect(context, this.x, this.y, this.width, this.height, bgColor);

        // Верхний блик (glass эффект)
        int highlightHeight = this.height / 2;
        drawRoundedRect(context, this.x, this.y, this.width, highlightHeight, 0x20FFFFFF);

        // Внутренняя тень для глубины
        if (!isEnabled) {
            context.fill(this.x + 2, this.y + 2, this.x + this.width - 2, this.y + 3, 0x30000000);
        }

        // Анимированная ручка (кружок)
        int toggleSize = this.height - 4;
        int togglePadding = 2;
        int maxOffset = this.width - toggleSize - (togglePadding * 2);
        int toggleX = this.x + togglePadding + (int)(maxOffset * animationProgress);
        int toggleY = this.y + togglePadding;

        // Увеличение ручки при наведении
        int hoverSize = (int)(toggleSize + hoverAnimation * 2);
        int hoverOffset = (toggleSize - hoverSize) / 2;
        toggleX += hoverOffset;
        toggleY += hoverOffset;

        // Тень ручки
        drawCircle(context, toggleX + 1, toggleY + 1, hoverSize, 0x60000000);

        // Ручка с glass эффектом
        drawCircle(context, toggleX, toggleY, hoverSize, 0xFFFFFFFF);

        // Блик на ручке (верхняя половина)
        int blickSize = hoverSize / 2;
        drawCircle(context, toggleX, toggleY, blickSize, 0x40FFFFFF);

        // Рамка переключателя
        int borderColor;
        if (isEnabled) {
            borderColor = isHovered ? 0xFF5AD865 : 0x8034C759;
        } else {
            borderColor = isHovered ? 0x80FFFFFF : 0x40FFFFFF;
        }
        drawRoundedBorder(context, this.x, this.y, this.width, this.height, borderColor);

        // Индикатор состояния справа
        String stateText = isEnabled ? "ВКЛ" : "ВЫКЛ";
        int stateColor;
        if (isEnabled) {
            stateColor = 0xFF34C759; // iOS зеленый
        } else {
            stateColor = 0xFFFF3B30; // iOS красный
        }

        int stateX = this.x + this.width + 10;
        int stateY = this.y + this.height / 2 - ViewmodelScreen.mc.textRenderer.fontHeight / 2;

        // Glass подложка для текста состояния при наведении
        if (isHovered) {
            int textWidth = ViewmodelScreen.mc.textRenderer.getWidth(stateText);
            context.fill(stateX - 3, stateY - 2, stateX + textWidth + 3,
                    stateY + ViewmodelScreen.mc.textRenderer.fontHeight + 2, 0x30FFFFFF);
        }

        context.drawTextWithShadow(ViewmodelScreen.mc.textRenderer, stateText, stateX, stateY, stateColor);

        // Подсказка при наведении
        if (isHovered && !tooltip.isEmpty()) {
            renderTooltip(context, mouseX, mouseY);
        }
    }

    /**
     * Рисует подсказку
     */
    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        int tooltipWidth = ViewmodelScreen.mc.textRenderer.getWidth(tooltip);
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 20;

        if (tooltipX + tooltipWidth + 8 > ViewmodelScreen.mc.getWindow().getScaledWidth()) {
            tooltipX = mouseX - tooltipWidth - 10;
        }

        context.fill(tooltipX - 4, tooltipY - 3, tooltipX + tooltipWidth + 4,
                tooltipY + ViewmodelScreen.mc.textRenderer.fontHeight + 3, 0x90000000);
        context.fill(tooltipX - 4, tooltipY - 3, tooltipX + tooltipWidth + 4,
                tooltipY + 2, 0x30FFFFFF);

        drawBorder(context, tooltipX - 4, tooltipY - 3, tooltipWidth + 8,
                ViewmodelScreen.mc.textRenderer.fontHeight + 6, 0x60FFFFFF);

        context.drawTextWithShadow(ViewmodelScreen.mc.textRenderer, tooltip,
                tooltipX, tooltipY, 0xFFFFFFFF);
    }

    /**
     * Рисует закругленный прямоугольник (имитация)
     */
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        // Основной прямоугольник
        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + 2, y + height - 2, color);
        context.fill(x + width - 2, y + 2, x + width, y + height - 2, color);

        // Углы (упрощенные)
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }

    /**
     * Рисует круг (имитация через квадраты)
     */
    private void drawCircle(DrawContext context, int centerX, int centerY, int size, int color) {
        int radius = size / 2;

        // Основной квадрат
        context.fill(centerX + 2, centerY, centerX + size - 2, centerY + size, color);
        context.fill(centerX, centerY + 2, centerX + 2, centerY + size - 2, color);
        context.fill(centerX + size - 2, centerY + 2, centerX + size, centerY + size - 2, color);

        // Сглаживание углов
        context.fill(centerX + 1, centerY + 1, centerX + 2, centerY + 2, color);
        context.fill(centerX + size - 2, centerY + 1, centerX + size - 1, centerY + 2, color);
        context.fill(centerX + 1, centerY + size - 2, centerX + 2, centerY + size - 1, color);
        context.fill(centerX + size - 2, centerY + size - 2, centerX + size - 1, centerY + size - 1, color);
    }

    /**
     * Рисует закругленную рамку
     */
    private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Горизонтальные линии
        context.fill(x + 2, y, x + width - 2, y + 1, color);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, color);

        // Вертикальные линии
        context.fill(x, y + 2, x + 1, y + height - 2, color);
        context.fill(x + width - 1, y + 2, x + width, y + height - 2, color);

        // Угловые пиксели
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Contract(pure = true)
    @Override
    public final boolean isWithin(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y &&
                mouseX <= this.x + this.width && mouseY <= this.y + this.height;
    }

    @Override
    public boolean isDragging() {
        return false;
    }
}