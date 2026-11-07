/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import dynamiclabs.immersivefx.ImmersiveFX;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

final class MenuChooserScreen extends Screen {
    private static final Identifier VFX_IMAGE = Identifier.fromNamespaceAndPath(ImmersiveFX.MOD_ID, "textures/gui/menu/ivfx_settings.png");
    private static final Identifier FX_IMAGE = Identifier.fromNamespaceAndPath(ImmersiveFX.MOD_ID, "textures/gui/menu/ifx_settings.png");
    private static final float ASPECT = 720.0F / 512.0F;
    private static final float MAX_HEIGHT = 0.30F;
    private static final float MAX_WIDTH = 0.24F;
    private static final int BACKDROP = 0x90000000;
    private static final int EDGE = 0xFF1E1E1F;
    private static final int HOVER = 0xFFFFFFFF;

    private float scale = 1.0F;
    private int tileW;
    private int tileH;
    private int tileY;
    private int vfxX;
    private int fxX;
    private int frame;

    MenuChooserScreen() {
        super(Component.translatable("immersivefx.menu.chooser"));
    }

    @Override
    protected void init() {
        final int guiScale = this.minecraft.getWindow().getGuiScale();
        final int windowW = this.minecraft.getWindow().getWidth();
        final int windowH = this.minecraft.getWindow().getHeight();
        scale = 1.0F / guiScale;
        frame = Math.max(2, guiScale);
        final int gap = Math.max(frame * 4, windowW / 80);
        tileW = Math.min(Math.round(windowW * MAX_WIDTH), Math.round(windowH * MAX_HEIGHT * ASPECT));
        tileH = Math.round(tileW / ASPECT);
        tileY = (windowH - tileH) / 2;
        vfxX = (windowW - gap) / 2 - tileW;
        fxX = (windowW + gap) / 2;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float a) {
        if (this.minecraft.level == null)
            this.extractPanorama(g, a);
        this.minecraft.gui.hud.extractDeferredSubtitles();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean over(final double mouseX, final double mouseY, final int tileX) {
        final double x = mouseX / scale;
        final double y = mouseY / scale;
        return x >= tileX && x < tileX + tileW && y >= tileY && y < tileY + tileH;
    }

    private void tile(final GuiGraphicsExtractor g, final Identifier image, final int x, final boolean hovered, final GpuSampler sampler) {
        final int width = hovered ? frame : 1;
        final int color = hovered ? HOVER : EDGE;
        g.fill(x - width, tileY - width, x + tileW + width, tileY + tileH + width, color);
        final AbstractTexture texture = this.minecraft.getTextureManager().getTexture(image);
        g.blit(texture.getTextureView(), sampler, x, tileY, x + tileW, tileY + tileH, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float a) {
        g.fill(0, 0, this.width, this.height, BACKDROP);
        final boolean vfxHovered = over(mouseX, mouseY, vfxX);
        final boolean fxHovered = over(mouseX, mouseY, fxX);
        final GpuSampler sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        g.pose().pushMatrix();
        g.pose().scale(scale, scale);
        tile(g, VFX_IMAGE, vfxX, vfxHovered, sampler);
        tile(g, FX_IMAGE, fxX, fxHovered, sampler);
        g.pose().popMatrix();
        if (vfxHovered || fxHovered)
            g.requestCursor(CursorTypes.POINTING_HAND);
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT)
            return false;
        final Screen target;
        if (over(event.x(), event.y(), vfxX))
            target = MenuOpener.vfxScreen(null);
        else if (over(event.x(), event.y(), fxX))
            target = new ImmersiveFXScreen(null);
        else
            return false;
        if (target == null)
            return true;
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        this.minecraft.gui.setScreen(target);
        return true;
    }
}
