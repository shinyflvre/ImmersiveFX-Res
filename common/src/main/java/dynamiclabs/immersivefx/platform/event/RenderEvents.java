/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform.event;

public final class RenderEvents {
    private RenderEvents() {
    }

    public static final EventHook<RenderFogEvent> RENDER_FOG = new EventHook<>("render fog");
    public static final EventHook<AfterSkyEvent> AFTER_SKY = new EventHook<>("after sky");
    public static final EventHook<ExtractLevelRenderStateEvent> EXTRACT_LEVEL_RENDER_STATE = new EventHook<>("extract level render state");
    public static final EventHook<SubmitCustomGeometryEvent> SUBMIT_CUSTOM_GEOMETRY = new EventHook<>("submit custom geometry");
}
