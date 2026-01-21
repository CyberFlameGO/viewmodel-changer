package net.cyberflame.viewmodel.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.cyberflame.viewmodel.gui.Switch;
import net.cyberflame.viewmodel.gui.ViewmodelGuiObj;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public non-sealed class BooleanSetting implements Setting<Boolean> {

    private Boolean value;
    private final String name;

    @Contract(pure = true)
    BooleanSetting(String settingName, boolean defaultValue) {
        super();
        this.name = settingName;
        this.value = defaultValue;
    }

    @Contract(value = " -> new", pure = true)
    @Override
    public final @NotNull JsonElement toJson() {
        return new JsonPrimitive(this.value);
    }

    @Contract(pure = true)
    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void setValue(@NotNull JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            this.value = element.getAsBoolean();
        }
    }

    @Contract(mutates = "this")
    @Override
    public final void setValue(Boolean value) {
        this.value = value;
    }

    @Contract(pure = true)
    @Override
    public final Boolean getValue() {
        return this.value;
    }

    @Override
    public final void createUIElementWithTooltip(@NotNull Collection<? super ViewmodelGuiObj> objs,
                                                 int settingIndex, int x, int y,
                                                 int width, int height, String tooltip) {
        int switchSize = Math.max(12, height);
        Switch switchObj = new Switch(this, x, y, switchSize);
        switchObj.setTooltip(tooltip);
        objs.add(switchObj);
    }
}