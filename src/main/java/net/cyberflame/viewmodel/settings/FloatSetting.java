package net.cyberflame.viewmodel.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.cyberflame.viewmodel.gui.Slider;
import net.cyberflame.viewmodel.gui.ViewmodelGuiObj;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public non-sealed class FloatSetting implements Setting<Float> {

    private Float value;
    private final String name;
    private final float min, max;

    @Contract(pure = true)
    FloatSetting(String settingName, float defaultValue, float min, float max) {
        super();
        this.name = settingName;
        this.value = defaultValue;
        this.min = min;
        this.max = max;
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
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            this.value = element.getAsFloat();
        }
    }

    @Contract(mutates = "this")
    @Override
    public final void setValue(Float value) {
        this.value = value;
    }

    @Contract(pure = true)
    @Override
    public final Float getValue() {
        return this.value;
    }

    @Contract(pure = true)
    public final float getMin() {
        return this.min;
    }

    @Contract(pure = true)
    public final float getMax() {
        return this.max;
    }

    @Override
    public final void createUIElementWithTooltip(@NotNull Collection<? super ViewmodelGuiObj> objs,
                                                 int settingIndex, int x, int y,
                                                 int width, int height, String tooltip) {
        int sliderWidth = Math.min(width, 200);
        int sliderHeight = Math.max(12, height);
        Slider slider = new Slider(this, x, y, sliderWidth, sliderHeight);
        slider.setTooltip(tooltip);
        objs.add(slider);
    }
}