/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class MenuStyle {
    static final int RAIL = 0xFF313233;
    static final int PANEL = 0xFF48494A;
    static final int TITLE_BAR = 0xFFE6E8EB;
    static final int TITLE_EDGE = 0xFFB1B2B5;
    static final int OUTLINE = 0xFF1E1E1F;
    static final int LINE = 0xFF313233;
    static final int GROOVE = 0xFF5A5B5C;
    static final int HOVER = 0xFF525354;
    static final int RAIL_HOVER = 0xFF3C3D3E;
    static final int SELECTED = 0xFF48494A;
    static final int TEXT = 0xFFFFFFFF;
    static final int TEXT_SOFT = 0xFFD0D1D4;
    static final int TEXT_GROUP = 0xFFB1B2B5;
    static final int TEXT_OFF = 0xFF8E8F91;
    static final int INK = 0xFF1E1E1F;
    static final int WELL = 0xFF1E1E1F;
    static final int WELL_HOVER = 0xFF2A2B2C;
    static final int ACCENT = 0xFF007AFF;
    static final int ACCENT_LIGHT = 0xFF3D9BFF;
    static final int ACCENT_DARK = 0xFF0057B8;
    static final int ACCENT_HOVER = 0xFF2B90FF;
    static final int ACCENT_OFF = 0xFF3F5A78;
    static final int LIGHT_FACE = 0xFFD0D1D4;
    static final int LIGHT_TOP = 0xFFF0F1F3;
    static final int LIGHT_LIP = 0xFF7B7C7E;
    static final int LIGHT_HOVER = 0xFFE6E8EB;
    static final int OFF_FACE = 0xFF58585A;
    static final int OFF_TOP = 0xFF6A6B6D;
    static final int OFF_LIP = 0xFF3C3D3E;
    static final int THUMB_BAR = 0xFFB1B2B5;
    static final int INVALID = 0xFFFF5555;

    static final int ICON_SOUND = 0;
    static final int ICON_SOUND_LIST = 1;
    static final int ICON_AMBIENCE = 2;
    static final int ICON_FOOTSTEPS = 3;
    static final int ICON_MOBS = 4;
    static final int ICON_PARTICLES = 5;
    static final int ICON_ATMOSPHERE = 6;
    static final int ICON_LIGHT = 7;
    static final int ICON_SPECIAL = 8;
    static final int ICON_ADVANCED = 9;
    static final int ICON_SEARCH = 10;
    static final int ICON_CLOSE = 11;
    static final int ICON_RESET = 12;
    static final int ICON_CHEVRON = 13;
    static final int ICON_PLUS = 14;

    private static final Identifier ICONS = Identifier.fromNamespaceAndPath(ImmersiveFX.MOD_ID, "textures/gui/menu/icons.png");

    private MenuStyle() {
    }

    static void icon(final GuiGraphicsExtractor g, final int index, final int x, final int y, final int color) {
        g.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, (index % 4) * 16, (index / 4) * 16, 16, 16, 64, 64, color);
    }

    static void rect(final GuiGraphicsExtractor g, final int x, final int y, final int w, final int h, final int color) {
        g.fill(x, y, x + w, y + h, color);
    }

    static void block(final GuiGraphicsExtractor g, final int x, final int y, final int w, final int h, final int face, final int top, final int lip) {
        rect(g, x, y, w, h, OUTLINE);
        rect(g, x + 1, y + 1, w - 2, h - 2, face);
        rect(g, x + 1, y + 1, w - 2, 1, top);
        rect(g, x + 1, y + h - 2, w - 2, 1, lip);
    }

    static void light(final GuiGraphicsExtractor g, final int x, final int y, final int w, final int h, final boolean hovered, final boolean enabled) {
        if (enabled)
            block(g, x, y, w, h, hovered ? LIGHT_HOVER : LIGHT_FACE, LIGHT_TOP, LIGHT_LIP);
        else
            block(g, x, y, w, h, OFF_FACE, OFF_TOP, OFF_LIP);
    }

    static void accent(final GuiGraphicsExtractor g, final int x, final int y, final int w, final int h, final boolean hovered, final boolean enabled) {
        if (enabled)
            block(g, x, y, w, h, hovered ? ACCENT_HOVER : ACCENT, ACCENT_LIGHT, ACCENT_DARK);
        else
            block(g, x, y, w, h, ACCENT_OFF, ACCENT_OFF, ACCENT_OFF);
    }

    static void well(final GuiGraphicsExtractor g, final int x, final int y, final int w, final int h, final int face) {
        rect(g, x, y, w, h, OUTLINE);
        rect(g, x + 1, y + 1, w - 2, h - 2, face);
    }

    static boolean inside(final double mx, final double my, final int x, final int y, final int w, final int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
