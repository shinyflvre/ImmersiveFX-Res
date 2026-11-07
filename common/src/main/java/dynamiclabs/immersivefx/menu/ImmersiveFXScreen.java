/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import dynamiclabs.immersivefx.ImmersiveFX;
import dynamiclabs.immersivefx.sndctrl.library.IndividualSoundConfig;
import dynamiclabs.immersivefx.sndctrl.library.SoundLibrary;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ImmersiveFXScreen extends Screen implements MenuRow.Host {
    private static final String MENU = "immersivefx.menu.";
    private static final int MIN_VIRTUAL_W = 640;
    private static final int TITLE_H = 24;
    private static final int CONTROL_H = 16;
    private static final int PAGE_H = 30;
    private static final int TAB_H = 18;
    private static final int GROUP_H = 13;
    private static final int RAIL_WIDE = 116;
    private static final int RAIL_NARROW = 24;
    private static final int POPUP_ROW = 14;
    private static final int SCROLL_STEP = 28;
    private static final int TOOLTIP_OFFSET = 12;

    private final @Nullable Screen parent;
    private final List<MenuCatalog.Page> pages;
    private final MenuCatalog.Page soundPage;
    private final List<MenuRow> visible = new ArrayList<>();
    private final MenuRow.TextInput searchInput = new MenuRow.TextInput() {
        @Override
        public String text() {
            return search;
        }

        @Override
        public void text(final String value) {
            search = value;
            changedOnly = false;
            searchChanged();
        }

        @Override
        public boolean accepts(final String typed) {
            return true;
        }

        @Override
        public void submit() {
            focus(null);
        }
    };
    private MenuCatalog.Page selected;
    private String search = "";
    private boolean changedOnly;
    private @Nullable MenuRow.TextInput focus;
    private @Nullable MenuRow dragging;
    private @Nullable MenuRow.Options popup;
    private @Nullable List<Component> tooltip;
    private boolean quiet;
    private int popupX;
    private int popupY;
    private int popupW;
    private float scale = 1.0F;
    private int canvasW;
    private int canvasH;
    private int panelW;
    private int railW;
    private boolean wide;
    private int bodyY;
    private int contentX;
    private int contentW;
    private int rowsY;
    private int rowsH;
    private int contentHeight;
    private int changed;
    private float scroll;
    private float scrollTarget;
    private float indicator = -1.0F;
    private long lastFrame;

    public ImmersiveFXScreen(final @Nullable Screen parent) {
        super(Component.translatable(MENU + "title"));
        this.parent = parent;
        this.pages = new MenuCatalog(this, previewAllowed()).pages();
        this.selected = pages.getFirst();
        MenuCatalog.Page sounds = selected;
        for (final MenuCatalog.Page page : pages)
            if (page.id().equals(MenuCatalog.SOUND_LIST))
                sounds = page;
        this.soundPage = sounds;
    }

    private static boolean previewAllowed() {
        final var minecraft = net.minecraft.client.Minecraft.getInstance();
        final IntegratedServer server = minecraft.getSingleplayerServer();
        return minecraft.level != null && minecraft.hasSingleplayerServer() && server != null && !server.isPublished();
    }

    static ModConfigSpec.ValueSpec specOf(final ModConfigSpec.ConfigValue<?> value) {
        return ImmersiveFX.SPEC.getSpec().get(value.getPath());
    }

    static <T> void commit(final ModConfigSpec.ConfigValue<T> value, final T target) {
        for (int attempt = 0; attempt < 2; attempt++) {
            value.set(target);
            ImmersiveFX.SPEC.save();
            if (target.equals(value.getRaw()))
                break;
        }
    }

    static <T> void preview(final ModConfigSpec.ConfigValue<T> value, final T target) {
        value.set(target);
    }

    @Override
    public Font font() {
        return this.font;
    }

    @Override
    public void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public void openOptions(final MenuRow.Options options, final int x, final int y, final int w) {
        popup = options;
        popupX = x;
        popupW = w;
        final int height = options.labels().size() * POPUP_ROW + 2;
        popupY = y + height > canvasH - 2 ? Math.max(2, y - height - CONTROL_H + 2) : y;
    }

    @Override
    public void focus(final @Nullable MenuRow.TextInput input) {
        if (focus == input)
            return;
        final boolean had = focus != null;
        focus = input;
        if (had != (input != null))
            this.minecraft.onTextInputFocusChange(this, input != null);
    }

    @Override
    public boolean focused(final MenuRow.TextInput input) {
        return focus == input;
    }

    @Override
    public void tooltip(final List<Component> lines) {
        tooltip = lines;
    }

    @Override
    public void soundsEdited() {
        final List<IndividualSoundConfig> edited = new ArrayList<>();
        for (final MenuRow row : soundPage.rows())
            if (row instanceof SoundRow sound && !sound.config.isDefault())
                edited.add(sound.config);
        SoundLibrary.updateSoundConfigurations(edited);
    }

    @Override
    protected void init() {
        final int guiScale = this.minecraft.getWindow().getGuiScale();
        final int zoom = Mth.clamp(this.minecraft.getWindow().getWidth() / MIN_VIRTUAL_W, 1, guiScale);
        scale = zoom / (float) guiScale;
        canvasW = (int) Math.ceil(this.width / scale);
        canvasH = (int) Math.ceil(this.height / scale);
        panelW = Math.min(Math.max(224, Math.round(canvasW * 0.34F)), Math.max(224, Math.min(560, Math.round(canvasW * 0.40F))));
        wide = panelW >= 360;
        railW = wide ? RAIL_WIDE : RAIL_NARROW;
        bodyY = TITLE_H + CONTROL_H + 6 + 1;
        contentX = railW;
        contentW = panelW - railW;
        rowsY = bodyY + PAGE_H;
        rowsH = canvasH - rowsY;
        lastFrame = 0L;
        popup = null;
        changed = changedCount();
        rebuildVisible();
    }

    private String query() {
        return search.trim().toLowerCase(Locale.ROOT);
    }

    private boolean filtering() {
        return changedOnly || !query().isEmpty();
    }

    private boolean soundsOnly() {
        return selected == soundPage && !changedOnly && !query().isEmpty();
    }

    private void rebuildVisible() {
        visible.clear();
        if (!filtering()) {
            visible.addAll(selected.rows());
        } else if (soundsOnly()) {
            final String query = query();
            for (final MenuRow row : soundPage.rows())
                if (row.matches(query))
                    visible.add(row);
        } else {
            for (final MenuCatalog.Page page : pages)
                if (page != soundPage)
                    addMatches(page);
            addMatches(soundPage);
        }
        relayout();
    }

    private void addMatches(final MenuCatalog.Page page) {
        final String query = query();
        final List<MenuRow> matches = new ArrayList<>();
        for (final MenuRow row : page.rows()) {
            if (!row.interactive())
                continue;
            if (changedOnly && !row.changed())
                continue;
            if (query.isEmpty() || row.matches(query))
                matches.add(row);
        }
        if (!matches.isEmpty()) {
            visible.add(new MenuRow.Section(this, page.title(), false));
            visible.addAll(matches);
        }
    }

    @Override
    public void relayout() {
        int y = 0;
        final int inner = contentW - 4;
        for (final MenuRow row : visible) {
            row.x = contentX;
            row.y = y;
            y += row.measure(inner);
        }
        contentHeight = y + 8;
        scrollTarget = Mth.clamp(scrollTarget, 0.0F, maxScroll());
    }

    private float maxScroll() {
        return Math.max(0, contentHeight - rowsH);
    }

    private int changedCount() {
        int count = 0;
        for (final MenuCatalog.Page page : pages)
            for (final MenuRow row : page.rows())
                if (row.changed())
                    count++;
        return count;
    }

    private void select(final MenuCatalog.Page page) {
        if (selected == soundPage && page != soundPage)
            leaveSounds();
        selected = page;
        if (page == soundPage && previewAllowed() && !quiet) {
            quiet = true;
            this.minecraft.getSoundManager().pauseAllExcept();
        }
        search = "";
        changedOnly = false;
        focus(null);
        scroll = 0.0F;
        scrollTarget = 0.0F;
        rebuildVisible();
    }

    private void leaveSounds() {
        for (final MenuRow row : soundPage.rows())
            if (row instanceof SoundRow sound)
                sound.stop();
        if (quiet) {
            quiet = false;
            this.minecraft.getSoundManager().resume();
        }
    }

    private double virtualX(final double x) {
        return x / scale;
    }

    private double virtualY(final double y) {
        return y / scale;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float a) {
        if (this.minecraft.level == null)
            this.extractPanorama(g, a);
        this.minecraft.gui.hud.extractDeferredSubtitles();
    }

    @Override
    public boolean isPauseScreen() {
        return selected == soundPage && previewAllowed();
    }

    @Override
    public void tick() {
        for (final MenuRow row : soundPage.rows())
            if (row instanceof SoundRow sound)
                sound.tick();
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor g, final int mouseX, final int mouseY, final float a) {
        final long now = System.nanoTime();
        final float dt = lastFrame == 0L ? 0.016F : Math.min(0.05F, (now - lastFrame) / 1.0E9F);
        lastFrame = now;
        scroll += (scrollTarget - scroll) * Math.min(1.0F, dt * 18.0F);
        if (Math.abs(scrollTarget - scroll) < 0.25F)
            scroll = scrollTarget;
        changed = changedCount();
        tooltip = null;
        final int mx = (int) Math.floor(virtualX(mouseX));
        final int my = (int) Math.floor(virtualY(mouseY));
        final boolean base = popup == null && mx < panelW;
        g.pose().pushMatrix();
        g.pose().scale(scale, scale);
        drawFrame(g);
        drawHeader(g, mx, my, base);
        drawRail(g, mx, my, base, dt, mouseX, mouseY);
        drawPage(g, mx, my, base, dt);
        g.pose().popMatrix();
        if (popup != null) {
            g.nextStratum();
            g.pose().pushMatrix();
            g.pose().scale(scale, scale);
            drawPopup(g, mx, my);
            g.pose().popMatrix();
        } else if (tooltip != null) {
            g.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY + TOOLTIP_OFFSET);
        }
    }

    private void drawFrame(final GuiGraphicsExtractor g) {
        MenuStyle.rect(g, 0, 0, panelW, canvasH, MenuStyle.PANEL);
        MenuStyle.rect(g, 0, bodyY, railW, canvasH - bodyY, MenuStyle.RAIL);
        MenuStyle.rect(g, 0, 0, panelW, TITLE_H - 2, MenuStyle.TITLE_BAR);
        MenuStyle.rect(g, 0, TITLE_H - 2, panelW, 1, MenuStyle.TITLE_EDGE);
        MenuStyle.rect(g, 0, TITLE_H - 1, panelW, 1, MenuStyle.OUTLINE);
        MenuStyle.rect(g, 0, bodyY - 2, panelW, 1, MenuStyle.LINE);
        MenuStyle.rect(g, 0, bodyY - 1, panelW, 1, MenuStyle.GROOVE);
        MenuStyle.rect(g, railW - 1, bodyY, 1, canvasH - bodyY, MenuStyle.OUTLINE);
        MenuStyle.rect(g, panelW, 0, 1, canvasH, MenuStyle.OUTLINE);
        for (int i = 0; i < 6; i++)
            MenuStyle.rect(g, panelW + 1 + i, 0, 1, canvasH, (0x48 - i * 0x0C) << 24);
    }

    private int searchWidth() {
        return panelW - 16 - chipWidth() - 4;
    }

    private void drawHeader(final GuiGraphicsExtractor g, final int mx, final int my, final boolean base) {
        final Component heading = this.title.copy().withStyle(ChatFormatting.BOLD);
        g.text(font, heading, (panelW - font.width(heading)) / 2, 7, MenuStyle.INK, false);
        final boolean closeHit = base && MenuStyle.inside(mx, my, panelW - 19, 3, 16, 16);
        MenuStyle.icon(g, MenuStyle.ICON_CLOSE, panelW - 19, 3, closeHit ? MenuStyle.ACCENT : MenuStyle.INK);
        if (closeHit)
            g.requestCursor(CursorTypes.POINTING_HAND);
        final int line = TITLE_H + 3;
        final int searchW = searchWidth();
        final boolean searchHit = base && MenuStyle.inside(mx, my, 8, line, searchW, CONTROL_H);
        MenuRow.field(g, font, searchInput, focus == searchInput, searchHit, 8, line, searchW, CONTROL_H, 17, Component.translatable(MENU + "search"), 0);
        MenuStyle.icon(g, MenuStyle.ICON_SEARCH, 9, line, MenuStyle.TEXT_SOFT);
        final int chipW = chipWidth();
        final int chipX = panelW - 8 - chipW;
        final boolean chipOn = changedOnly || changed > 0;
        final boolean chipHit = base && chipOn && MenuStyle.inside(mx, my, chipX, line, chipW, CONTROL_H);
        if (changedOnly)
            MenuStyle.accent(g, chipX, line, chipW, CONTROL_H, chipHit, true);
        else
            MenuStyle.light(g, chipX, line, chipW, CONTROL_H, chipHit, chipOn);
        final Component chip = changedLabel(changed);
        final int chipColor = changedOnly ? MenuStyle.TEXT : chipOn ? MenuStyle.INK : MenuStyle.TEXT_OFF;
        g.text(font, chip, chipX + (chipW - font.width(chip)) / 2, line + 4, chipColor, false);
        if (chipHit)
            g.requestCursor(CursorTypes.POINTING_HAND);
    }

    private Component changedLabel(final int count) {
        return Component.translatable(MENU + "changed", count);
    }

    private int chipWidth() {
        return font.width(changedLabel(changed)) + 10;
    }

    private int groupCount() {
        int count = 0;
        String group = null;
        for (final MenuCatalog.Page page : pages) {
            if (!page.group().equals(group)) {
                group = page.group();
                count++;
            }
        }
        return count;
    }

    private void drawRail(final GuiGraphicsExtractor g, final int mx, final int my, final boolean base, final float dt, final int realX, final int realY) {
        final int doneH = wide ? 18 : 20;
        final int bottom = canvasH - doneH - 6;
        final boolean tight = bottom - bodyY < pages.size() * TAB_H + (wide ? groupCount() * GROUP_H : (groupCount() - 1) * 4) + 4;
        final int pitch = tight ? Math.max(14, (bottom - bodyY - 4) / pages.size()) : TAB_H;
        int y = bodyY + 2;
        String group = null;
        int selectedY = y;
        final boolean tabShown = !filtering() || soundsOnly();
        for (final MenuCatalog.Page page : pages) {
            if (!tight && !page.group().equals(group)) {
                if (wide) {
                    g.text(font, MenuRow.trim(font, Component.translatable(MENU + "group." + page.group()), railW - 12), 7, y + 3, MenuStyle.TEXT_GROUP, false);
                    y += GROUP_H;
                } else if (group != null) {
                    MenuStyle.rect(g, 4, y + 1, railW - 8, 1, MenuStyle.GROOVE);
                    y += 4;
                }
                group = page.group();
            }
            final boolean current = page == selected && tabShown;
            final boolean hit = base && MenuStyle.inside(mx, my, 0, y, railW - 1, pitch);
            if (current) {
                MenuStyle.rect(g, 0, y, railW - 1, pitch, MenuStyle.SELECTED);
                selectedY = y;
            } else if (hit) {
                MenuStyle.rect(g, 0, y, railW - 1, pitch, MenuStyle.RAIL_HOVER);
            }
            final int iconColor = current || hit ? MenuStyle.TEXT : MenuStyle.TEXT_SOFT;
            final int iconX = wide ? 5 : (railW - 1 - 16) / 2;
            MenuStyle.icon(g, page.icon(), iconX, y + (pitch - 16) / 2, iconColor);
            if (wide)
                g.text(font, MenuRow.trim(font, page.title(), railW - 34), 24, y + (pitch - 8) / 2, current || hit ? MenuStyle.TEXT : MenuStyle.TEXT_SOFT, false);
            if (page.anyChanged())
                MenuStyle.rect(g, railW - 7, y + pitch / 2 - 1, 3, 3, MenuStyle.ACCENT_LIGHT);
            if (hit) {
                g.requestCursor(CursorTypes.POINTING_HAND);
                if (!wide)
                    g.setTooltipForNextFrame(font, page.title(), realX, realY);
            }
            y += pitch;
        }
        if (tabShown) {
            final float goal = selectedY;
            indicator = indicator < 0.0F ? goal : indicator + (goal - indicator) * Math.min(1.0F, dt * 20.0F);
            MenuStyle.rect(g, 0, Math.round(indicator), 3, pitch, MenuStyle.ACCENT);
        }
        final int doneX = 4;
        final int doneW = railW - 9;
        final int doneY = canvasH - doneH - 4;
        final boolean doneHit = base && MenuStyle.inside(mx, my, doneX, doneY, doneW, doneH);
        MenuStyle.accent(g, doneX, doneY, doneW, doneH, doneHit, true);
        if (wide) {
            final Component done = Component.translatable(MENU + "done").withStyle(ChatFormatting.BOLD);
            g.text(font, done, doneX + (doneW - font.width(done)) / 2, doneY + 5, MenuStyle.TEXT, false);
        } else {
            MenuStyle.icon(g, MenuStyle.ICON_CLOSE, doneX + (doneW - 16) / 2, doneY + 2, MenuStyle.TEXT);
        }
        if (doneHit)
            g.requestCursor(CursorTypes.POINTING_HAND);
    }

    private void drawPage(final GuiGraphicsExtractor g, final int mx, final int my, final boolean base, final float dt) {
        final Component pageTitle;
        final Component subtitle;
        if (filtering()) {
            int found = 0;
            for (final MenuRow row : visible)
                if (row.interactive())
                    found++;
            pageTitle = Component.translatable(MENU + (changedOnly ? "changedTitle" : "searchTitle"));
            subtitle = Component.translatable(MENU + "found", found);
        } else {
            pageTitle = selected.title();
            subtitle = selected.subtitle();
        }
        final int textX = contentX + MenuRow.PAD_X;
        final int textW = contentW - MenuRow.PAD_X * 2;
        g.text(font, pageTitle.copy().withStyle(ChatFormatting.BOLD), textX, bodyY + 6, MenuStyle.TEXT, false);
        g.text(font, MenuRow.trim(font, subtitle, textW), textX, bodyY + 17, MenuStyle.TEXT_SOFT, false);
        MenuStyle.rect(g, contentX, rowsY - 2, contentW, 1, MenuStyle.LINE);
        MenuStyle.rect(g, contentX, rowsY - 1, contentW, 1, MenuStyle.GROOVE);
        final int shift = Math.round(scroll);
        final boolean pointer = base && my >= rowsY && mx >= contentX;
        g.enableScissor(contentX, rowsY, panelW, canvasH);
        for (final MenuRow row : visible) {
            final int saved = row.y;
            row.y = saved + rowsY - shift;
            if (row.y + row.h > rowsY && row.y < canvasH) {
                row.paint(g, mx, my, pointer && dragging == null || row == dragging, dt);
                if (row instanceof MenuRow.Section section)
                    section.section(g);
            }
            row.y = saved;
        }
        if (visible.isEmpty()) {
            final Component none = Component.translatable(MENU + (changedOnly ? "noneChanged" : "noResults"));
            for (final var line : font.split(none, textW))
                g.text(font, line, textX, rowsY + 10, MenuStyle.TEXT_SOFT, false);
        }
        g.disableScissor();
        final float max = maxScroll();
        if (max > 0.0F) {
            final int barH = Math.max(14, Math.round(rowsH * rowsH / (float) contentHeight));
            final int barY = rowsY + Math.round((rowsH - barH) * (scroll / max));
            MenuStyle.rect(g, panelW - 4, rowsY, 3, rowsH, MenuStyle.LINE);
            MenuStyle.rect(g, panelW - 4, barY, 3, barH, MenuStyle.THUMB_BAR);
        }
    }

    private void drawPopup(final GuiGraphicsExtractor g, final int mx, final int my) {
        final List<Component> labels = popup.labels();
        final int height = labels.size() * POPUP_ROW + 2;
        MenuStyle.rect(g, popupX - 1, popupY - 1, popupW + 2, height + 2, MenuStyle.OUTLINE);
        MenuStyle.rect(g, popupX, popupY, popupW, height, MenuStyle.RAIL);
        final int current = popup.selected();
        for (int i = 0; i < labels.size(); i++) {
            final int y = popupY + 1 + i * POPUP_ROW;
            final boolean hit = MenuStyle.inside(mx, my, popupX, y, popupW, POPUP_ROW);
            if (i == current)
                MenuStyle.rect(g, popupX, y, popupW, POPUP_ROW, MenuStyle.SELECTED);
            else if (hit)
                MenuStyle.rect(g, popupX, y, popupW, POPUP_ROW, MenuStyle.HOVER);
            if (i == current)
                MenuStyle.rect(g, popupX, y, 2, POPUP_ROW, MenuStyle.ACCENT);
            g.text(font, MenuRow.trim(font, labels.get(i), popupW - 10), popupX + 5, y + 3, hit || i == current ? MenuStyle.TEXT : MenuStyle.TEXT_SOFT, false);
            if (hit)
                g.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        final double mx = virtualX(event.x());
        final double my = virtualY(event.y());
        if (popup != null) {
            final int height = popup.labels().size() * POPUP_ROW + 2;
            if (MenuStyle.inside(mx, my, popupX, popupY, popupW, height)) {
                final int index = (int) ((my - popupY - 1) / POPUP_ROW);
                if (index >= 0 && index < popup.labels().size()) {
                    popup.select(index);
                    click();
                }
            }
            popup = null;
            return true;
        }
        if (mx >= panelW) {
            focus(null);
            return false;
        }
        final boolean left = event.button() == InputConstants.MOUSE_BUTTON_LEFT;
        final int line = TITLE_H + 3;
        if (left && MenuStyle.inside(mx, my, 8, line, searchWidth(), CONTROL_H)) {
            focus(searchInput);
            return true;
        }
        focus(null);
        if (!left && my < rowsY)
            return true;
        if (MenuStyle.inside(mx, my, panelW - 19, 3, 16, 16)) {
            click();
            onClose();
            return true;
        }
        final int chipW = chipWidth();
        final int chipX = panelW - 8 - chipW;
        if (MenuStyle.inside(mx, my, chipX, line, chipW, CONTROL_H)) {
            if (changedOnly || changedCount() > 0) {
                changedOnly = !changedOnly;
                search = "";
                scrollTarget = 0.0F;
                scroll = 0.0F;
                click();
                rebuildVisible();
            }
            return true;
        }
        if (mx < railW)
            return clickRail(mx, my);
        if (my >= rowsY) {
            final double contentY = my - rowsY + Math.round(scroll);
            for (final MenuRow row : visible) {
                if (contentY >= row.y && contentY < row.y + row.h) {
                    if (row.press(mx, contentY, event.button())) {
                        if (row.capturesDrag())
                            dragging = row;
                        if (filtering() && changedOnly)
                            rebuildVisible();
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private boolean clickRail(final double mx, final double my) {
        final int doneH = wide ? 18 : 20;
        final int doneY = canvasH - doneH - 4;
        if (MenuStyle.inside(mx, my, 4, doneY, railW - 9, doneH)) {
            click();
            onClose();
            return true;
        }
        final int bottom = canvasH - doneH - 6;
        final boolean tight = bottom - bodyY < pages.size() * TAB_H + (wide ? groupCount() * GROUP_H : (groupCount() - 1) * 4) + 4;
        final int pitch = tight ? Math.max(14, (bottom - bodyY - 4) / pages.size()) : TAB_H;
        int y = bodyY + 2;
        String group = null;
        for (final MenuCatalog.Page page : pages) {
            if (!tight && !page.group().equals(group)) {
                if (wide)
                    y += GROUP_H;
                else if (group != null)
                    y += 4;
                group = page.group();
            }
            if (my >= y && my < y + pitch) {
                click();
                select(page);
                return true;
            }
            y += pitch;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(final MouseButtonEvent event, final double dx, final double dy) {
        if (dragging != null) {
            dragging.dragged(virtualX(event.x()));
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final MouseButtonEvent event) {
        if (dragging != null) {
            dragging.released();
            dragging = null;
            if (filtering() && changedOnly)
                rebuildVisible();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        if (popup == null && virtualX(x) < panelW) {
            scrollTarget = Mth.clamp(scrollTarget - (float) scrollY * SCROLL_STEP, 0.0F, maxScroll());
            return true;
        }
        return false;
    }

    private void searchChanged() {
        scroll = 0.0F;
        scrollTarget = 0.0F;
        rebuildVisible();
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (popup != null) {
            if (event.isEscape())
                popup = null;
            return true;
        }
        if (focus != null) {
            final String text = focus.text();
            if (event.isEscape()) {
                if (text.isEmpty())
                    focus(null);
                else
                    focus.text("");
                return true;
            }
            if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
                focus.submit();
                return true;
            }
            if (event.key() == InputConstants.KEY_BACKSPACE) {
                if (!text.isEmpty())
                    focus.text(event.hasControlDown() ? "" : text.substring(0, text.length() - 1));
                return true;
            }
            if (event.isPaste()) {
                final String pasted = this.minecraft.keyboardHandler.getClipboard().replace("\n", " ").trim();
                if (focus.accepts(pasted))
                    focus.text(text + pasted);
                return true;
            }
            if (event.isSelectAll() || event.isCut()) {
                focus.text("");
                return true;
            }
        }
        if (event.shortcutKey() == 102 && event.hasControlDownWithQuirk()) {
            focus(searchInput);
            return true;
        }
        if (event.key() == InputConstants.KEY_PAGEDOWN || event.key() == InputConstants.KEY_DOWN) {
            scrollTarget = Mth.clamp(scrollTarget + (event.key() == InputConstants.KEY_DOWN ? SCROLL_STEP : rowsH - 20), 0.0F, maxScroll());
            return true;
        }
        if (event.key() == InputConstants.KEY_PAGEUP || event.key() == InputConstants.KEY_UP) {
            scrollTarget = Mth.clamp(scrollTarget - (event.key() == InputConstants.KEY_UP ? SCROLL_STEP : rowsH - 20), 0.0F, maxScroll());
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(final CharacterEvent event) {
        if (focus != null && event.isAllowedChatCharacter()) {
            final String typed = event.codepointAsString();
            if (focus.accepts(typed))
                focus.text(focus.text() + typed);
            return true;
        }
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        focus(null);
        leaveSounds();
        ImmersiveFX.SPEC.save();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}
