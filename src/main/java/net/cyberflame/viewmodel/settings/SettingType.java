package net.cyberflame.viewmodel.settings;

import org.jetbrains.annotations.Contract;

/**
 * Перечисление всех доступных настроек для viewmodel.
 * Каждая рука настраивается отдельно для максимальной гибкости.
 */
public enum SettingType {

    // === ОСНОВНАЯ РУКА (Main Hand) ===

    // Включение/выключение настроек позиции основной руки
    MAIN_HAND_POS("Основная рука: Позиция", true),

    // Позиция основной руки по осям (X - влево/вправо, Y - вверх/вниз, Z - вперед/назад)
    MAIN_HAND_POS_X("└ Смещение X", 0.0f, -10.0f, 10.0f),
    MAIN_HAND_POS_Y("└ Смещение Y", 0.0f, -10.0f, 10.0f),
    MAIN_HAND_POS_Z("└ Смещение Z", 0.0f, -10.0f, 10.0f),

    // Включение/выключение настроек вращения основной руки
    MAIN_HAND_ROTATION("Основная рука: Вращение", false),

    // Вращение основной руки (X - pitch, Y - yaw, Z - roll)
    MAIN_HAND_ROTATION_X("└ Поворот X (Pitch)", 0.0f, -180.0f, 180.0f),
    MAIN_HAND_ROTATION_Y("└ Поворот Y (Yaw)", 0.0f, -180.0f, 180.0f),
    MAIN_HAND_ROTATION_Z("└ Поворот Z (Roll)", 0.0f, -180.0f, 180.0f),

    // === ВТОРАЯ РУКА (Off Hand) ===

    // Включение/выключение настроек позиции второй руки
    OFF_HAND_POS("Вторая рука: Позиция", true),

    // Позиция второй руки по осям
    OFF_HAND_POS_X("└ Смещение X", 0.0f, -10.0f, 10.0f),
    OFF_HAND_POS_Y("└ Смещение Y", 0.0f, -10.0f, 10.0f),
    OFF_HAND_POS_Z("└ Смещение Z", 0.0f, -10.0f, 10.0f),

    // Включение/выключение настроек вращения второй руки
    OFF_HAND_ROTATION("Вторая рука: Вращение", false),

    // Вращение второй руки
    OFF_HAND_ROTATION_X("└ Поворот X (Pitch)", 0.0f, -180.0f, 180.0f),
    OFF_HAND_ROTATION_Y("└ Поворот Y (Yaw)", 0.0f, -180.0f, 180.0f),
    OFF_HAND_ROTATION_Z("└ Поворот Z (Roll)", 0.0f, -180.0f, 180.0f);

    private final Setting<?> setting;

    // Конструктор для boolean настроек
    SettingType(String name, boolean defaultValue) {
        this.setting = new BooleanSetting(name, defaultValue);
    }

    // Конструктор для float настроек с диапазоном
    SettingType(String name, float defaultValue, float min, float max) {
        this.setting = new FloatSetting(name, defaultValue, min, max);
    }

    @Contract(pure = true)
    public Setting<?> getSetting() {
        return this.setting;
    }

    /**
     * Проверяет, является ли настройка булевой и включена ли она.
     */
    public boolean isTrue() {
        if (this.setting instanceof BooleanSetting boolSetting) {
            return boolSetting.getValue();
        }
        return false;
    }

    /**
     * Получает значение float настройки.
     */
    public float getFloatValue() {
        if (this.setting instanceof FloatSetting floatSetting) {
            return floatSetting.getValue();
        }
        return 0.0f;
    }
}