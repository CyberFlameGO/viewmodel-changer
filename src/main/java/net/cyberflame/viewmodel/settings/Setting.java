package net.cyberflame.viewmodel.settings;

import com.google.gson.JsonElement;
import net.cyberflame.viewmodel.gui.ViewmodelGuiObj;

import java.util.Collection;

/**
 * Интерфейс для всех типов настроек viewmodel.
 */
public sealed interface Setting<T> permits BooleanSetting, FloatSetting {

    String getName();

    T getValue();

    void setValue(T val);

    void setValue(JsonElement element);

    JsonElement toJson();

    /**
     * Создает UI элемент без подсказки (обратная совместимость)
     */
    default void createUIElement(Collection<? super ViewmodelGuiObj> objs, int settingIndex,
                                 int centerX, int currentY, int width, int height) {
        createUIElementWithTooltip(objs, settingIndex, centerX, currentY, width, height, "");
    }

    /**
     * Создает UI элемент с подсказкой
     */
    void createUIElementWithTooltip(Collection<? super ViewmodelGuiObj> objs, int settingIndex,
                                    int centerX, int currentY, int width, int height, String tooltip);
}