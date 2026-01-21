package net.cyberflame.viewmodel.gui;

import net.cyberflame.viewmodel.Viewmodel;
import net.cyberflame.viewmodel.settings.BooleanSetting;
import net.cyberflame.viewmodel.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Современный экран настроек viewmodel в стиле iOS с эффектом Liquid Glass.
 * Две колонки: левая для основной руки, правая для второй руки.
 */
public class ViewmodelScreen extends Screen {

    static final MinecraftClient mc = MinecraftClient.getInstance();
    private final Collection<ViewmodelGuiObj> leftHandObjs = new ArrayList<>();
    private final Collection<ViewmodelGuiObj> rightHandObjs = new ArrayList<>();
    private final Collection<ViewmodelGuiObj> generalObjs = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxScroll = 0;

    // Адаптивные размеры
    private int scaledPadding;
    private int scaledSpacing;
    private int columnWidth;
    private int centerGap;

    public ViewmodelScreen() {
        super(Text.of("Viewmodel Settings"));
    }

    @Override
    public final void init() {
        leftHandObjs.clear();
        rightHandObjs.clear();
        generalObjs.clear();

        calculateScaledSizes();

        List<Setting<?>> settingsList = Viewmodel.getSettings();

        // Определяем, какая рука основная
        Arm mainArm = mc.options.getMainArm().getValue();
        boolean isRightHanded = mainArm == Arm.RIGHT;

        int leftX = this.width / 2 - columnWidth - centerGap / 2;
        int rightX = this.width / 2 + centerGap / 2;

        int leftY = scaledPadding + 60;
        int rightY = scaledPadding + 60;

        // ЛЕВАЯ КОЛОНКА - Основная рука
        leftY = createHandColumn(settingsList, leftHandObjs, leftX, leftY, 0, 9,
                isRightHanded ? "ПРАВАЯ РУКА (Основная)" : "ЛЕВАЯ РУКА (Основная)");

        // ПРАВАЯ КОЛОНКА - Вторая рука
        rightY = createHandColumn(settingsList, rightHandObjs, rightX, rightY, 9, 18,
                isRightHanded ? "ЛЕВАЯ РУКА (Вторая)" : "ПРАВАЯ РУКА (Вторая)");

        // Вычисляем максимальную прокрутку
        maxScroll = Math.max(0, Math.max(leftY, rightY) - this.height + scaledPadding);
    }

    private void calculateScaledSizes() {
        int guiScale = (int) this.client.getWindow().getScaleFactor();
        scaledPadding = Math.max(15, 25 / guiScale);
        scaledSpacing = Math.max(22, 32 / guiScale); // Увеличено с 28 до 32
        columnWidth = Math.min(280, this.width / 2 - 40);
        centerGap = Math.max(20, 30 / guiScale);
    }

    /**
     * Создает колонку настроек для одной руки
     */
    private int createHandColumn(List<Setting<?>> settings, Collection<ViewmodelGuiObj> objs,
                                 int x, int startY, int fromIndex, int toIndex, String title) {
        int currentY = startY;

        for (int i = fromIndex; i < toIndex && i < settings.size(); i++) {
            Setting<?> setting = settings.get(i);
            String name = setting.getName();

            // Убираем префиксы из названий
            name = name.replace("Основная рука: ", "").replace("Вторая рука: ", "");

            int elementHeight = scaledSpacing - 4;
            setting.createUIElementWithTooltip(objs, i, x, currentY + scrollOffset,
                    columnWidth, elementHeight, getTooltipForSetting(name));

            if (setting instanceof BooleanSetting) {
                currentY += scaledSpacing + 8;
            } else {
                currentY += scaledSpacing + 2;
            }
        }

        return currentY;
    }

    /**
     * Создает секцию общих настроек
     */
    private int createGeneralSettings(List<Setting<?>> settings, Collection<ViewmodelGuiObj> objs,
                                      int x, int startY, int fromIndex) {
        int currentY = startY;

        for (int i = fromIndex; i < settings.size(); i++) {
            Setting<?> setting = settings.get(i);
            int elementHeight = scaledSpacing - 4;
            setting.createUIElementWithTooltip(objs, i, x, currentY + scrollOffset,
                    columnWidth, elementHeight, getTooltipForSetting(setting.getName()));
            currentY += scaledSpacing + 2;
        }

        return currentY;
    }

    /**
     * Возвращает подсказку для настройки
     */
    private String getTooltipForSetting(String settingName) {
        return switch (settingName) {
            case "Позиция" -> "Включить настройку позиции руки";
            case "└ Смещение X" -> "← Влево / Вправо →";
            case "└ Смещение Y" -> "↓ Вниз / Вверх ↑";
            case "└ Смещение Z" -> "← Ближе / Дальше →";
            case "Вращение" -> "Включить вращение руки";
            case "└ Поворот X (Pitch)" -> "↑ Наклон вверх/вниз ↓";
            case "└ Поворот Y (Yaw)" -> "← Поворот влево/вправо →";
            case "└ Поворот Z (Roll)" -> "⟲ Крен (наклон в стороны) ⟳";
            case "Масштаб" -> "Включить изменение масштаба руки";
            case "└ Масштаб X" -> "Ширина руки";
            case "└ Масштаб Y" -> "Высота руки";
            case "└ Масштаб Z" -> "Глубина руки";
            case "Изменить анимацию взмаха" -> "Отключить стандартную анимацию удара";
            default -> "";
        };
    }

    @Override
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Простой фон без blur эффекта (ещё светлее)
        context.fill(0, 0, this.width, this.height, 0xC0202020);

        // Определяем названия колонок
        Arm mainArm = mc.options.getMainArm().getValue();
        boolean isRightHanded = mainArm == Arm.RIGHT;

        String leftTitle = isRightHanded ? "ПРАВАЯ РУКА" : "ЛЕВАЯ РУКА";
        String leftSubtitle = "(Основная)";
        String rightTitle = isRightHanded ? "ЛЕВАЯ РУКА" : "ПРАВАЯ РУКА";
        String rightSubtitle = "(Вторая)";

        int leftX = this.width / 2 - columnWidth - centerGap / 2;
        int rightX = this.width / 2 + centerGap / 2;
        int headerY = scaledPadding + 45;

        // Заголовки колонок с glass эффектом
        drawColumnHeader(context, leftX, headerY, columnWidth, leftTitle, leftSubtitle, 0x4044AA44);
        drawColumnHeader(context, rightX, headerY, columnWidth, rightTitle, rightSubtitle, 0x404488EE);

        // Рендер элементов
        leftHandObjs.forEach(obj -> obj.render(context, mouseX, mouseY));
        rightHandObjs.forEach(obj -> obj.render(context, mouseX, mouseY));

        // Подсказка внизу
        drawBottomHint(context);

        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Рисует заголовок колонки с glass эффектом
     */
    private void drawColumnHeader(DrawContext context, int x, int y, int width,
                                  String title, String subtitle, int accentColor) {
        // Glass card
        drawGlassCard(context, x, y, width, subtitle.isEmpty() ? 22 : 28, 0x25FFFFFF, accentColor);

        // Текст заголовка
        int titleWidth = mc.textRenderer.getWidth(title);
        context.drawTextWithShadow(mc.textRenderer, title,
                x + (width - titleWidth) / 2, y + 4, 0xFFFFFFFF);

        // Подзаголовок
        if (!subtitle.isEmpty()) {
            int subtitleWidth = mc.textRenderer.getWidth(subtitle);
            context.drawTextWithShadow(mc.textRenderer, subtitle,
                    x + (width - subtitleWidth) / 2, y + 16, 0xFFAAAAAA);
        }
    }

    /**
     * Рисует glass card с эффектом жидкого стекла
     */
    private void drawGlassCard(DrawContext context, int x, int y, int width, int height,
                               int fillColor, int borderColor) {
        // Тень
        context.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x40000000);

        // Основной фон
        context.fill(x, y, x + width, y + height, fillColor);

        // Верхний блик (эффект стекла)
        context.fill(x, y, x + width, y + height / 3, 0x15FFFFFF);

        // Рамка
        drawRoundedBorder(context, x, y, width, height, borderColor);
    }

    /**
     * Рисует закругленную рамку
     */
    private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Верхняя линия
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        // Нижняя линия
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        // Левая линия
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        // Правая линия
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /**
     * Рисует подсказку внизу экрана
     */
    private void drawBottomHint(DrawContext context) {
        String hint = "ESC - Закрыть  |  Колесо мыши - Точная настройка  |  ЛКМ - Перетащить";
        int hintWidth = mc.textRenderer.getWidth(hint);
        int hintX = (this.width - hintWidth) / 2;
        int hintY = this.height - 20;

        // Glass подложка для подсказки
        drawGlassCard(context, hintX - 10, hintY - 5, hintWidth + 20, 18, 0x30000000, 0x40FFFFFF);
        context.drawTextWithShadow(mc.textRenderer, hint, hintX, hintY, 0xFFAAAAAA);
    }

    @Override
    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ViewmodelGuiObj obj : leftHandObjs) {
            if (obj.isWithin(mouseX, mouseY)) {
                obj.mouseClicked(mouseX, mouseY);
                return true;
            }
        }
        for (ViewmodelGuiObj obj : rightHandObjs) {
            if (obj.isWithin(mouseX, mouseY)) {
                obj.mouseClicked(mouseX, mouseY);
                return true;
            }
        }
        for (ViewmodelGuiObj obj : generalObjs) {
            if (obj.isWithin(mouseX, mouseY)) {
                obj.mouseClicked(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        leftHandObjs.forEach(obj -> obj.mouseReleased(mouseX, mouseY));
        rightHandObjs.forEach(obj -> obj.mouseReleased(mouseX, mouseY));
        generalObjs.forEach(obj -> obj.mouseReleased(mouseX, mouseY));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int button,
                                      double deltaX, double deltaY) {
        for (ViewmodelGuiObj obj : leftHandObjs) {
            if (obj.isWithin(mouseX, mouseY) || obj.isDragging()) {
                obj.mouseDragged(mouseX, mouseY, deltaX, deltaY);
                return true;
            }
        }
        for (ViewmodelGuiObj obj : rightHandObjs) {
            if (obj.isWithin(mouseX, mouseY) || obj.isDragging()) {
                obj.mouseDragged(mouseX, mouseY, deltaX, deltaY);
                return true;
            }
        }
        for (ViewmodelGuiObj obj : generalObjs) {
            if (obj.isWithin(mouseX, mouseY) || obj.isDragging()) {
                obj.mouseDragged(mouseX, mouseY, deltaX, deltaY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        // Проверяем, не над элементом ли мы
        boolean overElement = false;

        for (ViewmodelGuiObj obj : leftHandObjs) {
            if (obj.isWithin(mouseX, mouseY)) {
                obj.mouseScrolled(mouseX, mouseY, (float) amountX, (float) amountY);
                return true; // Сразу возвращаем true, не продолжаем
            }
        }

        for (ViewmodelGuiObj obj : rightHandObjs) {
            if (obj.isWithin(mouseX, mouseY)) {
                obj.mouseScrolled(mouseX, mouseY, (float) amountX, (float) amountY);
                return true; // Сразу возвращаем true
            }
        }

        // Если не над элементом, скроллим весь экран
        int scrollSpeed = scaledSpacing;
        scrollOffset += (int) (amountY * scrollSpeed);
        scrollOffset = Math.max(-maxScroll, Math.min(0, scrollOffset));
        init();

        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}