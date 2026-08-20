package hw.smoup.schat.client.chat;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

public final class SettingsSlider extends AbstractSliderButton {

    private final DoubleFunction<String> label;
    private final DoubleConsumer sink;

    public SettingsSlider(int x, int y, int width, int height, double initial,
                          DoubleFunction<String> label, DoubleConsumer sink) {
        super(x, y, width, height, Component.empty(), initial);
        this.label = label;
        this.sink = sink;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (label != null) {
            setMessage(Component.literal(label.apply(value)));
        }
    }

    @Override
    protected void applyValue() {
        if (sink != null) {
            sink.accept(value);
        }
    }
}
