package net.cyberflame.viewmodel.settings;

public enum SettingType {
    // Main hand
    MAIN_HAND_ENABLED    ("viewmodel.setting.main_hand_enabled", true),
    MAIN_HAND_POS_X      ("viewmodel.setting.pos_x", 0f, -50f, 50f),
    MAIN_HAND_POS_Y      ("viewmodel.setting.pos_y", 0f, -10f, 10f),
    MAIN_HAND_POS_Z      ("viewmodel.setting.pos_z", 0f, -50f, 50f),
    MAIN_HAND_ROT_X      ("viewmodel.setting.rot_x", 0f, -180f, 180f),
    MAIN_HAND_ROT_Y      ("viewmodel.setting.rot_y", 0f, -180f, 180f),
    MAIN_HAND_ROT_Z      ("viewmodel.setting.rot_z", 0f, -180f, 180f),

    // Off hand
    OFF_HAND_ENABLED     ("viewmodel.setting.off_hand_enabled", false),
    OFF_HAND_POS_X       ("viewmodel.setting.pos_x", 0f, -50f, 50f),
    OFF_HAND_POS_Y       ("viewmodel.setting.pos_y", 0f, -10f, 10f),
    OFF_HAND_POS_Z       ("viewmodel.setting.pos_z", 0f, -50f, 50f),
    OFF_HAND_ROT_X       ("viewmodel.setting.rot_x", 0f, -180f, 180f),
    OFF_HAND_ROT_Y       ("viewmodel.setting.rot_y", 0f, -180f, 180f),
    OFF_HAND_ROT_Z       ("viewmodel.setting.rot_z", 0f, -180f, 180f);

    private final Setting<?> setting;
    private final String langKey;

    SettingType(String langKey, boolean def) {
        this.langKey = langKey;
        this.setting = new BooleanSetting(name(), def);
    }

    SettingType(String langKey, float def, float min, float max) {
        this.langKey = langKey;
        this.setting = new FloatSetting(name(), def, min, max);
    }

    public Setting<?> getSetting() { return setting; }
    public String getLangKey()     { return langKey; }
    public boolean isEnabled()     { return ((BooleanSetting) setting).getValue(); }
    public float getValue()        { return ((FloatSetting) setting).getValue(); }
}