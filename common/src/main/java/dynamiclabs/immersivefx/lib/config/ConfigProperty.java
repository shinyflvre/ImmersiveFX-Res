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

package dynamiclabs.immersivefx.lib.config;

import dynamiclabs.immersivefx.lib.gui.GuiHelpers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class ConfigProperty {

    public static final int TOOLTIP_WIDTH = 300;

    private final ModConfigSpec.ValueSpec valueSpec;
    private final String name;

    private Component[] toolTip;

    private ConfigProperty(@Nonnull final ModConfigSpec.ConfigValue<?> configEntry) {
        final List<String> path = configEntry.getPath();
        this.valueSpec = configEntry.getSpec();
        this.name = path.get(path.size() - 1);
    }

    @Nullable
    public String getTranslationKey() {
        return this.valueSpec.getTranslationKey();
    }

    @Nonnull
    public MutableComponent getConfigName() {
        final String key = getTranslationKey();
        if (StringUtil.isNullOrEmpty(key)) {
            return Component.literal(this.name);
        }

        return Component.translatable(key);
    }

    @Nullable
    public String getComment() {
        return this.valueSpec.getComment();
    }

    @Nullable
    public Component[] getTooltip() {
        if (this.toolTip == null) {
            final List<Component> result = new ArrayList<>();
            String key = getTranslationKey();
            if (StringUtil.isNullOrEmpty(key)) {
                key = getComment();
                if (StringUtil.isNullOrEmpty(key))
                    return null;
                result.add(Component.literal(key));
            } else {
                final Component title = Component.literal(ChatFormatting.GOLD + Component.translatable(key).getString());
                result.add(title);
                result.addAll(GuiHelpers.getTrimmedTextCollection(key + ".tooltip", TOOLTIP_WIDTH));
            }

            final Object theDefault = getDefault();
            if (theDefault != null) {
                String text = theDefault.toString();
                if (text.compareToIgnoreCase("true") == 0)
                    text = CommonComponents.OPTION_ON.getString();
                else if (text.compareToIgnoreCase("false") == 0)
                    text = CommonComponents.OPTION_OFF.getString();
                else
                    text = GuiHelpers.getTrimmedText(text, TOOLTIP_WIDTH).getString();
                text = Component.translatable("dsurround.text.format.default", text).getString();
                result.add(Component.literal(text));
            }

            final Object range = this.valueSpec.getRange();
            if (range != null) {
                result.add(Component.literal(ChatFormatting.GREEN + "[ " + range + " ]"));
            }

            if (getNeedsWorldRestart()) {
                result.add(Component.translatable("dsurround.text.tooltip.restartRequired"));
            }

            this.toolTip = result.toArray(new Component[0]);
        }

        return this.toolTip;
    }

    public boolean getNeedsWorldRestart() {
        return this.valueSpec.restartType() != ModConfigSpec.RestartType.NONE;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefault() {
        return (T) this.valueSpec.getDefault();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMinValue() {
        final ModConfigSpec.Range<?> range = this.valueSpec.getRange();
        return range == null ? null : (T) range.getMin();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMaxValue() {
        final ModConfigSpec.Range<?> range = this.valueSpec.getRange();
        return range == null ? null : (T) range.getMax();
    }

    @Nonnull
    public static ConfigProperty getPropertyInfo(@Nonnull final ModConfigSpec.ConfigValue<?> configEntry) {
        return new ConfigProperty(configEntry);
    }

}
