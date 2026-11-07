/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dynamiclabs.immersivefx.lib.fml.ForgeUtils;
import dynamiclabs.immersivefx.lib.gui.GuiHelpers;
import dynamiclabs.immersivefx.sndctrl.api.sound.Category;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundCategory;
import dynamiclabs.immersivefx.sndctrl.api.sound.ISoundInstance;
import dynamiclabs.immersivefx.sndctrl.api.sound.SoundBuilder;
import dynamiclabs.immersivefx.sndctrl.audio.AudioEngine;
import dynamiclabs.immersivefx.sndctrl.audio.SoundMetadata;
import dynamiclabs.immersivefx.sndctrl.library.IndividualSoundConfig;
import dynamiclabs.immersivefx.sndctrl.library.SoundLibrary;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

final class SoundRow extends MenuRow {
    private static final int BUTTON_H = 14;
    private static final int BUTTON_PAD = 10;
    private static final int TRACK_H = 6;
    private static final int STEP = 5;
    private static final int TOOLTIP_WIDTH = 300;
    private static final ChatFormatting[] HELP_STYLE = {ChatFormatting.ITALIC, ChatFormatting.AQUA};
    private static final Component PLAY = Component.translatable("immersivefx.menu.sound.play");
    private static final Component STOP = Component.translatable("immersivefx.menu.sound.stop");
    private static final Component BLOCK = Component.translatable("immersivefx.menu.sound.block");
    private static final Component CULL = Component.translatable("immersivefx.menu.sound.cull");

    final IndividualSoundConfig config;
    private final IndividualSoundConfig fallback;
    private final boolean canPlay;
    private @Nullable ISoundInstance playing;
    private @Nullable List<Component> info;
    private boolean dragging;
    private int shown;

    SoundRow(final Host host, final IndividualSoundConfig config, final IndividualSoundConfig fallback, final boolean canPlay) {
        super(host, Component.literal(config.getLocation().toString()), Component.empty());
        this.config = config;
        this.fallback = fallback;
        this.canPlay = canPlay;
    }

    @Override
    int controlWidth(final int available) {
        return host.font().width(IndividualSoundConfig.VOLUME_SCALE_MAX + "%") + 2;
    }

    @Override
    int controlBelow() {
        return BUTTON_H + 2;
    }

    @Override
    boolean changed() {
        return config.isBlocked() != fallback.isBlocked() || config.isCulled() != fallback.isCulled() || config.getVolumeScaleInt() != fallback.getVolumeScaleInt();
    }

    @Override
    void reset() {
        config.setIsBlocked(fallback.isBlocked());
        config.setIsCulled(fallback.isCulled());
        config.setVolumeScaleInt(fallback.getVolumeScaleInt());
        host.soundsEdited();
    }

    @Override
    boolean capturesDrag() {
        return dragging;
    }

    private Component playLabel() {
        return playing != null ? STOP : PLAY;
    }

    private int buttonY() {
        return y + h - PAD_Y - BUTTON_H;
    }

    private int widthOf(final Component label) {
        return host.font().width(label) + BUTTON_PAD;
    }

    private int blockX() {
        return left() + widthOf(playLabel()) + 2;
    }

    private int cullX() {
        return blockX() + widthOf(BLOCK) + 2;
    }

    private int trackX() {
        return cullX() + widthOf(CULL) + 6;
    }

    private int trackY() {
        return buttonY() + (BUTTON_H - TRACK_H) / 2;
    }

    private void button(final GuiGraphicsExtractor g, final Font font, final Component label, final int bx, final boolean lit, final boolean enabled, final int mx, final int my,
                        final boolean pointer) {
        final int bw = widthOf(label);
        final int by = buttonY();
        final boolean hit = enabled && pointer && MenuStyle.inside(mx, my, bx, by, bw, BUTTON_H);
        if (lit)
            MenuStyle.accent(g, bx, by, bw, BUTTON_H, hit, enabled);
        else
            MenuStyle.light(g, bx, by, bw, BUTTON_H, hit, enabled);
        final int color = !enabled ? MenuStyle.TEXT_OFF : lit ? MenuStyle.TEXT : MenuStyle.INK;
        g.text(font, label, bx + BUTTON_PAD / 2, by + 3, color, false);
        if (hit)
            g.requestCursor(CursorTypes.POINTING_HAND);
    }

    @Override
    void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
        final Font font = host.font();
        final int value = dragging ? shown : config.getVolumeScaleInt();
        final String text = value + "%";
        g.text(font, text, right() - font.width(text), y + PAD_Y, MenuStyle.TEXT, false);
        button(g, font, playLabel(), left(), playing != null, canPlay, mx, my, pointer);
        button(g, font, BLOCK, blockX(), config.isBlocked(), true, mx, my, pointer);
        button(g, font, CULL, cullX(), config.isCulled(), true, mx, my, pointer);
        final int tx = trackX();
        final int tw = Math.max(8, right() - tx);
        final int ty = trackY();
        MenuStyle.well(g, tx, ty, tw, TRACK_H, MenuStyle.WELL_HOVER);
        final float t = value / (float) IndividualSoundConfig.VOLUME_SCALE_MAX;
        final int fill = Math.round((tw - 2) * t);
        if (fill > 0)
            MenuStyle.rect(g, tx + 1, ty + 1, fill, TRACK_H - 2, MenuStyle.ACCENT);
        final boolean hit = dragging || pointer && MenuStyle.inside(mx, my, tx, ty - 4, tw, TRACK_H + 8);
        MenuStyle.light(g, tx + Math.round((tw - 7) * t), ty - 3, 7, TRACK_H + 6, hit, true);
        if (hit)
            g.requestCursor(CursorTypes.RESIZE_EW);
        if (over && !dragging)
            host.tooltip(tooltip(mx, my));
    }

    private List<Component> tooltip(final int mx, final int my) {
        if (info == null) {
            final List<Component> base = new ArrayList<>();
            final Identifier location = config.getLocation();
            final String modName = ForgeUtils.getModDisplayName(location.getNamespace());
            base.add(Component.literal(modName).withStyle(ChatFormatting.GOLD));
            base.add(Component.literal(location.toString()).withStyle(ChatFormatting.GRAY));
            final SoundMetadata meta = SoundLibrary.getSoundMetadata(location);
            final Component soundTitle = meta.getTitle();
            if (!soundTitle.getString().isEmpty())
                base.add(soundTitle);
            final ISoundCategory category = meta.getCategory();
            if (category != Category.NEUTRAL)
                base.add(Component.translatable("sndctrl.text.tooltip.category").append(category.getTextComponent()));
            if (modName.equals("Minecraft"))
                base.add(Component.translatable("sndctrl.text.tooltip.vanilla"));
            else
                base.addAll(meta.getCredits());
            info = base;
        }
        final List<Component> lines = new ArrayList<>(info);
        final String help = helpKey(mx, my);
        if (help != null)
            lines.addAll(GuiHelpers.getTrimmedTextCollection(help, TOOLTIP_WIDTH, HELP_STYLE));
        return lines;
    }

    private @Nullable String helpKey(final int mx, final int my) {
        final int by = buttonY();
        if (MenuStyle.inside(mx, my, left(), by, widthOf(playLabel()), BUTTON_H))
            return "sndctrl.text.soundconfig.play.help";
        if (MenuStyle.inside(mx, my, blockX(), by, widthOf(BLOCK), BUTTON_H))
            return "sndctrl.text.soundconfig.block.help";
        if (MenuStyle.inside(mx, my, cullX(), by, widthOf(CULL), BUTTON_H))
            return "sndctrl.text.soundconfig.cull.help";
        if (MenuStyle.inside(mx, my, trackX(), by, right() - trackX(), BUTTON_H))
            return "sndctrl.text.soundconfig.volume.help";
        return null;
    }

    @Override
    boolean clicked(final double mx, final double my, final int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT)
            return false;
        final int by = buttonY();
        if (MenuStyle.inside(mx, my, left(), by, widthOf(playLabel()), BUTTON_H)) {
            if (canPlay)
                togglePlay();
            return canPlay;
        }
        if (MenuStyle.inside(mx, my, blockX(), by, widthOf(BLOCK), BUTTON_H)) {
            config.setIsBlocked(!config.isBlocked());
            host.click();
            host.soundsEdited();
            return true;
        }
        if (MenuStyle.inside(mx, my, cullX(), by, widthOf(CULL), BUTTON_H)) {
            config.setIsCulled(!config.isCulled());
            host.click();
            host.soundsEdited();
            return true;
        }
        if (MenuStyle.inside(mx, my, trackX() - 2, trackY() - 4, right() - trackX() + 4, TRACK_H + 8)) {
            dragging = true;
            shown = config.getVolumeScaleInt();
            dragged(mx);
            host.click();
            return true;
        }
        return false;
    }

    @Override
    void dragged(final double mx) {
        if (!dragging)
            return;
        final double t = Mth.clamp((mx - trackX() - 3) / Math.max(1.0, right() - trackX() - 7.0), 0.0, 1.0);
        final int raw = (int) Math.round(t * IndividualSoundConfig.VOLUME_SCALE_MAX);
        shown = Mth.clamp(Math.round(raw / (float) STEP) * STEP, IndividualSoundConfig.VOLUME_SCALE_MIN, IndividualSoundConfig.VOLUME_SCALE_MAX);
        config.setVolumeScaleInt(shown);
    }

    @Override
    void released() {
        if (!dragging)
            return;
        dragging = false;
        host.soundsEdited();
    }

    private void togglePlay() {
        if (playing != null) {
            stop();
            return;
        }
        SoundLibrary.getSound(config.getLocation()).ifPresent(event -> {
            final ISoundInstance instance = SoundBuilder.builder(event, Category.CONFIG).setGlobal(true).setVolume(config.getVolumeScale()).build();
            AudioEngine.play(instance);
            playing = instance;
        });
    }

    void stop() {
        if (playing != null) {
            AudioEngine.stop(playing);
            playing = null;
        }
    }

    void tick() {
        if (playing != null && playing.getState().isTerminal())
            playing = null;
    }
}
