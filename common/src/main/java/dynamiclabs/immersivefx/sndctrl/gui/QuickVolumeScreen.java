/*
 * ImmersiveFX
 * Originally from Dynamic Surroundings by OreCruncher, licensed under GPL-3.0.
 * Edited by Shinyflvres and contributors, 2026.
 */
/*
 * Dynamic Surroundings
 * Copyright (C) 2020  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package dynamiclabs.immersivefx.sndctrl.gui;

import dynamiclabs.immersivefx.lib.gui.ColorPalette;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import dynamiclabs.immersivefx.lib.gui.ValueSlider;
import net.neoforged.neoforge.common.ModConfigSpec;
import dynamiclabs.immersivefx.sndctrl.config.Config;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuickVolumeScreen extends Screen {

    private static final int CONTROL_WIDTH = 160;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_SPACING = 5;
    private static final Component SUFFIX = Component.literal("%");
    private static final Component FOOTER = Component.translatable("sndctrl.text.quickvolumemenu.footer");
    private static final Component TITLE = Component.translatable("sndctrl.text.quickvolumemenu.title");
    private static final Component OCCLUSION = Component.translatable("sndctrl.text.quickvolumemenu.occlusion");

    private final List<ISoundCategory> categories = new ArrayList<>();
    private final List<Float> categoryValues = new ArrayList<>();
    private final List<Slider> sliders = new ArrayList<>();

    private Button occlusionToggle;

    private int footerY;

    protected QuickVolumeScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {

        // Clear out the old cached data
        this.categories.clear();
        this.categoryValues.clear();
        this.sliders.clear();

        // Collect the widgets into a list
        this.categories.addAll(Category.getCategoriesForMenu());
        for (final ISoundCategory cat : this.categories)
            this.categoryValues.add(cat.getVolumeScale());

        // Get base positioning information for display.  This should be roughly center of the screen.
        final int leftSide = (this.width - CONTROL_WIDTH) / 2;

        // Calculate the top of where the first slider would be
        final int totalHeight = (this.categories.size() + 1) * (CONTROL_HEIGHT + CONTROL_SPACING);
        int top = (this.height - totalHeight) / 2;

        // Build slider widgets for them.
        for (final ISoundCategory category : this.categories) {
            final Slider slider = new Slider(
                    leftSide,
                    top,
                    CONTROL_WIDTH,
                    CONTROL_HEIGHT,
                    getSliderLabel(category.getTextComponent()),
                    SUFFIX,
                    (int) (category.getVolumeScale() * 100),
                    this::onChangeSliderValue
            );

            top += CONTROL_HEIGHT + CONTROL_SPACING;
            addRenderableWidget(slider);
            this.sliders.add(slider);
        }

        this.occlusionToggle = Button.builder(generateTextForSetting(Config.CLIENT.sound.enableOcclusionCalcs), this::buttonPress)
                .bounds(leftSide, top, CONTROL_WIDTH, CONTROL_HEIGHT)
                .build();
        addRenderableWidget(this.occlusionToggle);
        top += CONTROL_HEIGHT + CONTROL_SPACING;

        this.footerY = top;
    }

    protected void buttonPress(@Nonnull final Button button) {
        Config.CLIENT.sound.enableOcclusionCalcs.set(!Config.CLIENT.sound.enableOcclusionCalcs.get());
        Config.CLIENT.sound.enableOcclusionCalcs.save();
        this.occlusionToggle.setMessage(generateTextForSetting(Config.CLIENT.sound.enableOcclusionCalcs));
    }

    protected Component generateTextForSetting(@Nonnull final ModConfigSpec.BooleanValue value) {
        final MutableComponent txt = OCCLUSION.plainCopy().append(": ");
        if (value.get()) {
            txt.append(CommonComponents.OPTION_ON);
        } else {
            txt.append(CommonComponents.OPTION_OFF);
        }
        return txt;
    }

    protected Component getSliderLabel(@Nonnull final Component text) {
        return text.copy().append(Component.literal(": "));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (final Slider slider : this.sliders) {
            if (slider.isMouseOver(mouseX, mouseY)) {
                slider.setSliderValue(slider.getValue() + 5 * (scrollY > 0 ? 1 : -1));
                break;
            }
        }
        return false;
    }

    private void onChangeSliderValue(@Nonnull final Slider slider) {
        // Need to identify the ISoundCategory associated with the slider.
        int idx = 0;
        for (; idx < this.sliders.size(); idx++) {
            if (this.sliders.get(idx) == slider)
                break;
        }

        // Safety just in case
        if (idx >= this.sliders.size())
            return;

        // Cache the value so we can set all at once
        float v = slider.getValueInt() / 100F;
        this.categoryValues.set(idx, v);
    }

    @Override
    public void extractRenderState(@Nonnull final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        // Render our text footer
        graphics.centeredText(this.font, FOOTER, this.width / 2, this.footerY, ARGB.opaque(ColorPalette.WHITE.rgb()));
    }

    @Override
    public void onClose() {
        for (int i = 0; i < this.categories.size(); i++) {
            final ISoundCategory cat = this.categories.get(i);
            // Setting the value will trigger autosave of the config
            try {
                cat.setVolumeScale(this.categoryValues.get(i));
            } catch (@Nonnull final Throwable t) {
                SoundControl.LOGGER.error(t, "Error saving value for Sound Category %s", cat.getName());
            }
        }
        super.onClose();
    }

    private static final class Slider extends ValueSlider {
        private final Consumer<Slider> onChange;

        Slider(int x, int y, int width, int height, Component prefix, Component suffix, int currentValue, @Nonnull final Consumer<Slider> onChange) {
            super(x, y, width, height, prefix, suffix, 0, 100, currentValue, true);
            this.onChange = onChange;
        }

        @Override
        protected void applyValue() {
            if (this.onChange != null)
                this.onChange.accept(this);
        }
    }
}
