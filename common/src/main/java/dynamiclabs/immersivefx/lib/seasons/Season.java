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

package dynamiclabs.immersivefx.lib.seasons;

import dynamiclabs.immersivefx.lib.Localization;
import dynamiclabs.immersivefx.lib.compat.ModEnvironment;
import net.minecraft.world.level.Level;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.lib.Lib;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

public enum Season {

    NONE(SeasonType.NONE, SeasonSubType.NONE),

    EARLY_SPRING(SeasonType.SPRING, SeasonSubType.EARLY),
    MID_SPRING(SeasonType.SPRING, SeasonSubType.MID),
    LATE_SPRING(SeasonType.SPRING, SeasonSubType.LATE),

    EARLY_SUMMER(SeasonType.SUMMER, SeasonSubType.EARLY),
    MID_SUMMER(SeasonType.SUMMER, SeasonSubType.MID),
    LATE_SUMMER(SeasonType.SUMMER, SeasonSubType.LATE),

    EARLY_AUTUMN(SeasonType.AUTUMN, SeasonSubType.EARLY),
    MID_AUTUMN(SeasonType.AUTUMN, SeasonSubType.MID),
    LATE_AUTUMN(SeasonType.AUTUMN, SeasonSubType.LATE),

    EARLY_WINTER(SeasonType.WINTER, SeasonSubType.EARLY),
    MID_WINTER(SeasonType.WINTER, SeasonSubType.MID),
    LATE_WINTER(SeasonType.WINTER, SeasonSubType.LATE);

    private static final String FORMAT_NONE = SoundControl.MOD_ID + ".season.noseason";
    private static final String FORMAT_STRING = SoundControl.MOD_ID + ".season.format";
    private static final ISeasonHelper SEASON_HELPER;

    static {
        if (ModEnvironment.SereneSeasons.isLoaded())
            SEASON_HELPER = Season::getSereneSeason;
        else
            SEASON_HELPER = world -> Season.NONE;
    }

    private final SeasonType season;
    private final SeasonSubType subType;

    Season(@Nonnull final SeasonType type, @Nonnull final SeasonSubType subType) {
        this.season = type;
        this.subType = subType;
    }

    @Nonnull
    public SeasonType getType() {
        return this.season;
    }

    @Nonnull
    public SeasonSubType getSubType() {
        return this.subType;
    }

    @Nonnull
    public static Season getSeason(@Nonnull final Level world) {
        return SEASON_HELPER.getSeason(world);
    }

    private static Method seasonStateMethod;
    private static Method subSeasonMethod;
    private static boolean sereneFailed;

    @Nonnull
    private static Season getSereneSeason(@Nonnull final Level world) {
        if (sereneFailed || world == null)
            return Season.NONE;
        try {
            if (seasonStateMethod == null) {
                final Class<?> helper = Class.forName("sereneseasons.api.season.SeasonHelper");
                for (final Method m : helper.getMethods()) {
                    if ("getSeasonState".equals(m.getName()) && m.getParameterCount() == 1
                            && m.getParameterTypes()[0].isAssignableFrom(world.getClass())) {
                        seasonStateMethod = m;
                        break;
                    }
                }
                if (seasonStateMethod == null)
                    throw new NoSuchMethodException("SeasonHelper.getSeasonState");
            }
            final Object state = seasonStateMethod.invoke(null, world);
            if (state == null)
                return Season.NONE;
            if (subSeasonMethod == null)
                subSeasonMethod = Class.forName("sereneseasons.api.season.ISeasonState").getMethod("getSubSeason");
            final Object subSeason = subSeasonMethod.invoke(state);
            if (subSeason instanceof Enum<?> e) {
                try {
                    return Season.valueOf(e.name());
                } catch (@Nonnull final IllegalArgumentException ignore) {
                }
            }
        } catch (@Nonnull final Throwable t) {
            sereneFailed = true;
            Lib.LOGGER.warn("Unable to query SereneSeasons season state: %s", t.toString());
        }
        return Season.NONE;
    }

    @Nonnull
    public String getFormattedText() {
        if (this.season == SeasonType.NONE) {
            return Localization.load(FORMAT_NONE);
        }

        return Localization.format(
                FORMAT_STRING,
                this.subType.getFormattedText(),
                this.season.getFormattedText()
        );
    }

    private interface ISeasonHelper {
        Season getSeason(@Nonnull final Level world);
    }
}
