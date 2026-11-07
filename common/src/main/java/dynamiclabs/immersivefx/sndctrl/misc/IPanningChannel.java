/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.sndctrl.misc;

import dynamiclabs.immersivefx.sndctrl.audio.NaturalPanning;

import javax.annotation.Nonnull;

public interface IPanningChannel {

    @Nonnull
    NaturalPanning.State sndctrl_panState();

    int sndctrl_panSource();
}
