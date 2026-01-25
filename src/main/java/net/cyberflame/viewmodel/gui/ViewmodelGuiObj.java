package net.cyberflame.viewmodel.gui;

import net.minecraft.client.gui.DrawContext;

public interface ViewmodelGuiObj {

    void render(DrawContext context, int mouseX, int mouseY);

    void mouseClicked(double mouseX, double mouseY);

    void mouseReleased(double mouseX, double mouseY);

    void mouseDragged(double mouseX, double mouseY, double deltaX, double deltaY);

    void mouseScrolled(double mouseX, double mouseY, float incx, float incy);

    boolean isWithin(double mouseX, double mouseY);

    boolean isDragging();

    /**
     * Устанавливает подсказку для элемента
     */
    default void setTooltip(String tooltip) {
        // По умолчанию ничего не делает
    }

}