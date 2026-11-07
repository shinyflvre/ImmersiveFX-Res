/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.lib.gui;

import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ValueSlider extends AbstractSliderButton {

    protected final Component prefix;
    protected final Component suffix;
    protected final double minValue;
    protected final double maxValue;
    protected final double stepSize = 1D;
    protected final boolean drawString;
    private final DecimalFormat format = new DecimalFormat("0");

    public ValueSlider(final int x, final int y, final int width, final int height, @Nonnull final Component prefix, @Nonnull final Component suffix,
                       final double minValue, final double maxValue, final double currentValue, final boolean drawString) {
        super(x, y, width, height, Component.empty(), 0D);
        this.prefix = prefix;
        this.suffix = suffix;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.drawString = drawString;
        this.value = snapToNearest((currentValue - minValue) / (maxValue - minValue));
        this.updateMessage();
    }

    public double getValue() {
        return this.value * (this.maxValue - this.minValue) + this.minValue;
    }

    public long getValueLong() {
        return Math.round(this.getValue());
    }

    public int getValueInt() {
        return (int) this.getValueLong();
    }

    public void setSliderValue(final double value) {
        this.setValue((value - this.minValue) / (this.maxValue - this.minValue));
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    @Override
    protected void setValue(final double fractionalValue) {
        super.setValue(snapToNearest(fractionalValue));
    }

    @Override
    public boolean keyPressed(@Nonnull final KeyEvent keyEvent) {
        boolean left = keyEvent.isLeft();
        if (left || keyEvent.isRight()) {
            if (this.minValue > this.maxValue)
                left = !left;
            final float f = left ? -1F : 1F;
            this.setSliderValue(this.getValue() + f * this.stepSize);
        }
        return false;
    }

    private double snapToNearest(double value) {
        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);
        value = (this.stepSize * Math.round(value / this.stepSize));
        if (this.minValue > this.maxValue)
            value = Mth.clamp(value, this.maxValue, this.minValue);
        else
            value = Mth.clamp(value, this.minValue, this.maxValue);
        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    protected void updateMessage() {
        if (this.drawString)
            this.setMessage(Component.literal("").append(this.prefix).append(this.getValueString()).append(this.suffix));
        else
            this.setMessage(Component.empty());
    }

    @Override
    protected void applyValue() {
    }
}
