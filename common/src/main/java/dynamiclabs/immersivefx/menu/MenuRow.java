/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

abstract class MenuRow {
    static final int PAD_X = 8;
    static final int PAD_Y = 6;
    static final int CHILD = 10;
    static final int LINE_H = 10;
    static final int RESET = 16;

    interface Host {
        Font font();

        void click();

        void relayout();

        void openOptions(Options options, int x, int y, int w);

        void focus(@Nullable TextInput input);

        boolean focused(TextInput input);

        void tooltip(List<Component> lines);

        void soundsEdited();
    }

    interface Options {
        List<Component> labels();

        int selected();

        void select(int index);
    }

    interface TextInput {
        String text();

        void text(String value);

        boolean accepts(String typed);

        void submit();
    }

    final Host host;
    final Component title;
    final Component description;
    boolean child;
    boolean restart;
    @Nullable BooleanSupplier needs;
    @Nullable Component needsName;
    int x;
    int y;
    int w;
    int h;
    private float glow;
    private List<FormattedCharSequence> lines = List.of();

    MenuRow(final Host host, final Component title, final Component description) {
        this.host = host;
        this.title = title;
        this.description = description;
    }

    MenuRow child(final BooleanSupplier needs, final Component needsName) {
        this.child = true;
        this.needs = needs;
        this.needsName = needsName;
        return this;
    }

    MenuRow needs(final BooleanSupplier needs, final Component needsName) {
        this.needs = needs;
        this.needsName = needsName;
        return this;
    }

    MenuRow restart() {
        this.restart = true;
        return this;
    }

    final boolean active() {
        return needs == null || needs.getAsBoolean();
    }

    final int left() {
        return x + PAD_X + (child ? CHILD : 0);
    }

    final int right() {
        return x + w - PAD_X;
    }

    boolean interactive() {
        return true;
    }

    boolean showsTitle() {
        return true;
    }

    boolean changed() {
        return false;
    }

    void reset() {
    }

    boolean capturesDrag() {
        return false;
    }

    int controlWidth(final int available) {
        return 0;
    }

    int controlBelow() {
        return 0;
    }

    boolean matches(final String query) {
        return title.getString().toLowerCase(Locale.ROOT).contains(query) || description.getString().toLowerCase(Locale.ROOT).contains(query);
    }

    private Component describe() {
        if (!restart)
            return description;
        final Component note = Component.translatable("immersivefx.menu.restart").withColor(MenuStyle.ACCENT_LIGHT & 0xFFFFFF);
        return description.getString().isEmpty() ? note : description.copy().append(" ").append(note);
    }

    int measure(final int width) {
        w = width;
        final Font font = host.font();
        final int inner = right() - left();
        controlWidth(inner);
        final Component text = active() || needsName == null ? describe() : Component.translatable("immersivefx.menu.requires", needsName);
        lines = text.getString().isEmpty() ? List.of() : font.split(text, Math.max(40, inner));
        h = PAD_Y + 9 + lines.size() * LINE_H + (lines.isEmpty() ? 0 : 4) + controlBelow() + PAD_Y;
        return h;
    }

    final void paint(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final float dt) {
        final boolean over = pointer && MenuStyle.inside(mx, my, x, y, w, h);
        glow = Mth.clamp(glow + (over && interactive() ? dt * 12.0F : -dt * 9.0F), 0.0F, 1.0F);
        if (glow > 0.0F)
            MenuStyle.rect(g, x, y, w, h, (Math.round(glow * 255.0F) << 24) | (MenuStyle.HOVER & 0xFFFFFF));
        if (interactive()) {
            MenuStyle.rect(g, x + PAD_X, y + h - 2, w - PAD_X * 2, 1, MenuStyle.LINE);
            MenuStyle.rect(g, x + PAD_X, y + h - 1, w - PAD_X * 2, 1, MenuStyle.GROOVE);
        }
        if (child)
            MenuStyle.rect(g, x + PAD_X + 2, y, 1, h, MenuStyle.GROOVE);
        final boolean on = active();
        final Font font = host.font();
        final int control = controlWidth(right() - left());
        final int titleWidth = right() - left() - (control > 0 ? control + 8 : 0) - (changed() ? RESET : 0);
        if (showsTitle())
            g.text(font, trim(font, title, titleWidth), left(), y + PAD_Y, on ? MenuStyle.TEXT : MenuStyle.TEXT_OFF, false);
        int textY = y + PAD_Y + 13;
        for (final FormattedCharSequence line : lines) {
            g.text(font, line, left(), textY, on ? MenuStyle.TEXT_SOFT : MenuStyle.TEXT_OFF, false);
            textY += LINE_H;
        }
        draw(g, mx, my, pointer, over, dt);
        if (changed() && on && interactive()) {
            final int rx = resetX();
            final int ry = y + PAD_Y - 3;
            final boolean hit = pointer && MenuStyle.inside(mx, my, rx, ry, RESET, RESET);
            MenuStyle.icon(g, MenuStyle.ICON_RESET, rx, ry, hit ? MenuStyle.TEXT : MenuStyle.ACCENT_LIGHT);
            if (hit)
                g.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    int resetX() {
        final int control = controlWidth(right() - left());
        return right() - (control > 0 ? control + 4 : 0) - RESET;
    }

    static String trim(final Font font, final Component text, final int width) {
        final String plain = text.getString();
        if (font.width(plain) <= width)
            return plain;
        return font.plainSubstrByWidth(plain, Math.max(0, width - font.width("..."))) + "...";
    }

    static void field(final GuiGraphicsExtractor g, final Font font, final TextInput input, final boolean focused, final boolean hover, final int x, final int y, final int w,
                      final int h, final int inset, final @Nullable Component placeholder, final int border) {
        MenuStyle.well(g, x, y, w, h, focused || hover ? MenuStyle.WELL_HOVER : MenuStyle.WELL);
        if (border != 0)
            g.outline(x, y, w, h, border);
        else if (focused)
            g.outline(x, y, w, h, MenuStyle.ACCENT);
        final int textW = w - inset - 4;
        final int textY = y + (h - 8) / 2;
        final String text = input.text();
        if (text.isEmpty() && !focused && placeholder != null) {
            g.text(font, trim(font, placeholder, textW), x + inset, textY, MenuStyle.TEXT_OFF, false);
        } else {
            final String shown = font.plainSubstrByWidth(text, textW, true);
            g.text(font, shown, x + inset, textY, MenuStyle.TEXT, false);
            if (focused && (System.currentTimeMillis() / 500L) % 2L == 0L)
                MenuStyle.rect(g, x + inset + font.width(shown) + 1, textY - 1, 1, 10, MenuStyle.ACCENT_LIGHT);
        }
        if (hover)
            g.requestCursor(CursorTypes.IBEAM);
    }

    abstract void draw(GuiGraphicsExtractor g, int mx, int my, boolean pointer, boolean over, float dt);

    final boolean press(final double mx, final double my, final int button) {
        if (!active() || !interactive())
            return false;
        if (changed() && MenuStyle.inside(mx, my, resetX(), y + PAD_Y - 3, RESET, RESET)) {
            reset();
            host.click();
            host.relayout();
            return true;
        }
        return clicked(mx, my, button);
    }

    boolean clicked(final double mx, final double my, final int button) {
        return false;
    }

    void dragged(final double mx) {
    }

    void released() {
    }

    static final class Section extends MenuRow {
        private final boolean small;

        Section(final Host host, final Component title, final boolean small) {
            super(host, title, Component.empty());
            this.small = small;
        }

        @Override
        boolean interactive() {
            return false;
        }

        @Override
        boolean showsTitle() {
            return false;
        }

        @Override
        int measure(final int width) {
            w = width;
            h = small ? 18 : 24;
            return h;
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
        }

        void section(final GuiGraphicsExtractor g) {
            final Font font = host.font();
            final Component text = small ? title : title.copy().withStyle(ChatFormatting.BOLD);
            final int textY = y + h - 12;
            g.text(font, text, x + PAD_X, textY, small ? MenuStyle.TEXT_SOFT : MenuStyle.TEXT, false);
            final int start = x + PAD_X + font.width(text) + 6;
            if (start < right())
                MenuStyle.rect(g, start, textY + 4, right() - start, 1, small ? MenuStyle.LINE : MenuStyle.GROOVE);
        }
    }

    static final class Switch extends MenuRow {
        private static final int WIDTH = 28;
        private static final int HEIGHT = 14;
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;
        private final boolean fallback;
        private float knob = -1.0F;

        Switch(final Host host, final Component title, final Component description, final BooleanSupplier get, final Consumer<Boolean> set, final boolean fallback) {
            super(host, title, description);
            this.get = get;
            this.set = set;
            this.fallback = fallback;
        }

        @Override
        int controlWidth(final int available) {
            return WIDTH;
        }

        @Override
        boolean changed() {
            return get.getAsBoolean() != fallback;
        }

        @Override
        void reset() {
            set.accept(fallback);
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
            final boolean value = get.getAsBoolean();
            final boolean on = active();
            final float goal = value ? 1.0F : 0.0F;
            knob = knob < 0.0F ? goal : Mth.clamp(knob + Math.signum(goal - knob) * Math.min(Math.abs(goal - knob), dt * 9.0F), 0.0F, 1.0F);
            final int sx = right() - WIDTH;
            final int sy = y + PAD_Y - 3;
            final int fill = Math.round((WIDTH - 2) * knob);
            MenuStyle.well(g, sx, sy, WIDTH, HEIGHT, on ? MenuStyle.WELL_HOVER : MenuStyle.OFF_LIP);
            if (fill > 0)
                MenuStyle.rect(g, sx + 1, sy + 1, fill, HEIGHT - 2, on ? MenuStyle.ACCENT : MenuStyle.ACCENT_OFF);
            if (value)
                MenuStyle.rect(g, sx + 6, sy + 4, 1, 6, on ? MenuStyle.TEXT : MenuStyle.TEXT_OFF);
            else
                g.outline(sx + WIDTH - 9, sy + 4, 4, 6, on ? MenuStyle.TEXT_GROUP : MenuStyle.TEXT_OFF);
            final int kx = sx + Math.round((WIDTH - 14) * knob);
            MenuStyle.light(g, kx, sy, 14, HEIGHT, over && on, on);
            if (over && on)
                g.requestCursor(CursorTypes.POINTING_HAND);
        }

        @Override
        boolean clicked(final double mx, final double my, final int button) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT)
                return false;
            set.accept(!get.getAsBoolean());
            host.click();
            host.relayout();
            return true;
        }
    }

    static final class Slider extends MenuRow {
        private static final int TRACK_H = 6;
        private final IntSupplier get;
        private final IntConsumer live;
        private final IntConsumer commit;
        private final int fallback;
        private final int min;
        private final int max;
        private final int step;
        private final String unit;
        private boolean dragging;
        private int shown;

        Slider(final Host host, final Component title, final Component description, final IntSupplier get, final IntConsumer live, final IntConsumer commit, final int fallback,
               final int min, final int max, final int step, final String unit) {
            super(host, title, description);
            this.get = get;
            this.live = live;
            this.commit = commit;
            this.fallback = fallback;
            this.min = min;
            this.max = max;
            this.step = step;
            this.unit = unit;
        }

        @Override
        int controlWidth(final int available) {
            return host.font().width(max + unit) + 2;
        }

        @Override
        int controlBelow() {
            return 16;
        }

        @Override
        boolean changed() {
            return get.getAsInt() != fallback;
        }

        @Override
        void reset() {
            commit.accept(fallback);
        }

        @Override
        boolean capturesDrag() {
            return dragging;
        }

        private int trackY() {
            return y + h - PAD_Y - 7;
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
            final Font font = host.font();
            final boolean on = active();
            shown = dragging ? shown : get.getAsInt();
            final String text = shown + unit;
            g.text(font, text, right() - font.width(text), y + PAD_Y, on ? MenuStyle.TEXT : MenuStyle.TEXT_OFF, false);
            final int tx = left();
            final int tw = right() - left();
            final int ty = trackY();
            MenuStyle.well(g, tx, ty, tw, TRACK_H, on ? MenuStyle.WELL_HOVER : MenuStyle.OFF_LIP);
            final float t = (shown - min) / (float) (max - min);
            final int fill = Math.round((tw - 2) * t);
            if (fill > 0)
                MenuStyle.rect(g, tx + 1, ty + 1, fill, TRACK_H - 2, on ? MenuStyle.ACCENT : MenuStyle.ACCENT_OFF);
            final int stops = (max - min) / step;
            if (stops > 1 && stops <= 24)
                for (int i = 1; i < stops; i++)
                    MenuStyle.rect(g, tx + Math.round((tw - 1) * i / (float) stops), ty + 1, 1, TRACK_H - 2, MenuStyle.OUTLINE);
            final int kx = tx + Math.round((tw - 7) * t);
            final boolean hit = on && (dragging || pointer && MenuStyle.inside(mx, my, tx, ty - 4, tw, TRACK_H + 8));
            MenuStyle.light(g, kx, ty - 3, 7, TRACK_H + 6, hit, on);
            if (hit)
                g.requestCursor(CursorTypes.RESIZE_EW);
        }

        @Override
        boolean clicked(final double mx, final double my, final int button) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT || !MenuStyle.inside(mx, my, left() - 2, trackY() - 5, right() - left() + 4, TRACK_H + 10))
                return false;
            dragging = true;
            shown = get.getAsInt();
            dragged(mx);
            host.click();
            return true;
        }

        @Override
        void dragged(final double mx) {
            if (!dragging)
                return;
            final double t = Mth.clamp((mx - left() - 3) / Math.max(1.0, right() - left() - 7.0), 0.0, 1.0);
            final int raw = (int) Math.round(Mth.lerp(t, min, max));
            final int next = Mth.clamp(min + Math.round((raw - min) / (float) step) * step, min, max);
            if (next != shown) {
                shown = next;
                live.accept(next);
            }
        }

        @Override
        void released() {
            if (!dragging)
                return;
            dragging = false;
            commit.accept(shown);
            host.relayout();
        }
    }

    static final class Choice extends MenuRow implements Options {
        private static final int SEGMENT_PAD = 10;
        private static final int CONTROL_H = 15;
        private final List<Component> labels;
        private final IntSupplier get;
        private final IntConsumer set;
        private final int fallback;
        private boolean segmented;
        private int controlW;

        Choice(final Host host, final Component title, final Component description, final List<Component> labels, final IntSupplier get, final IntConsumer set, final int fallback) {
            super(host, title, description);
            this.labels = new ArrayList<>(labels);
            this.get = get;
            this.set = set;
            this.fallback = fallback;
        }

        @Override
        int controlWidth(final int available) {
            final Font font = host.font();
            int total = 0;
            int widest = 0;
            for (final Component label : labels) {
                final int width = font.width(label);
                total += width + SEGMENT_PAD;
                widest = Math.max(widest, width);
            }
            segmented = labels.size() <= 3 && total <= available * 0.6F;
            controlW = segmented ? total : Math.min(widest + 26, Math.round(available * 0.55F));
            return controlW;
        }

        @Override
        boolean changed() {
            return get.getAsInt() != fallback;
        }

        @Override
        void reset() {
            set.accept(fallback);
        }

        @Override
        public List<Component> labels() {
            return labels;
        }

        @Override
        public int selected() {
            return get.getAsInt();
        }

        @Override
        public void select(final int index) {
            set.accept(index);
            host.relayout();
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
            final Font font = host.font();
            final boolean on = active();
            final int cy = y + PAD_Y - 3;
            final int cx = right() - controlW;
            final int current = get.getAsInt();
            if (segmented) {
                int sx = cx;
                for (int i = 0; i < labels.size(); i++) {
                    final int sw = font.width(labels.get(i)) + SEGMENT_PAD;
                    final boolean hit = on && pointer && MenuStyle.inside(mx, my, sx, cy, sw, CONTROL_H);
                    if (i == current)
                        MenuStyle.accent(g, sx, cy, sw, CONTROL_H, hit, on);
                    else
                        MenuStyle.light(g, sx, cy, sw, CONTROL_H, hit, on);
                    final int color = !on ? MenuStyle.TEXT_OFF : i == current ? MenuStyle.TEXT : MenuStyle.INK;
                    g.text(font, labels.get(i), sx + SEGMENT_PAD / 2, cy + 4, color, false);
                    if (hit)
                        g.requestCursor(CursorTypes.POINTING_HAND);
                    sx += sw;
                }
                return;
            }
            final boolean hit = on && pointer && MenuStyle.inside(mx, my, cx, cy, controlW, CONTROL_H);
            MenuStyle.light(g, cx, cy, controlW, CONTROL_H, hit, on);
            g.text(font, trim(font, labels.get(Mth.clamp(current, 0, labels.size() - 1)), controlW - 22), cx + 5, cy + 4, on ? MenuStyle.INK : MenuStyle.TEXT_OFF, false);
            MenuStyle.icon(g, MenuStyle.ICON_CHEVRON, cx + controlW - 15, cy, on ? MenuStyle.INK : MenuStyle.TEXT_OFF);
            if (hit)
                g.requestCursor(CursorTypes.POINTING_HAND);
        }

        @Override
        boolean clicked(final double mx, final double my, final int button) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT)
                return false;
            final Font font = host.font();
            final int cy = y + PAD_Y - 3;
            final int cx = right() - controlW;
            if (!MenuStyle.inside(mx, my, cx, cy, controlW, CONTROL_H))
                return false;
            if (!segmented) {
                host.click();
                host.openOptions(this, cx, cy + CONTROL_H, controlW);
                return true;
            }
            int sx = cx;
            for (int i = 0; i < labels.size(); i++) {
                final int sw = font.width(labels.get(i)) + SEGMENT_PAD;
                if (mx >= sx && mx < sx + sw) {
                    if (i != get.getAsInt()) {
                        set.accept(i);
                        host.click();
                        host.relayout();
                    }
                    return true;
                }
                sx += sw;
            }
            return false;
        }
    }

    static final class NumberField extends MenuRow implements TextInput {
        private static final int WIDTH = 74;
        private static final int HEIGHT = 15;
        private static final int MAX_DIGITS = 10;
        private final IntSupplier get;
        private final IntConsumer set;
        private final int fallback;
        private String buffer;

        NumberField(final Host host, final Component title, final Component description, final IntSupplier get, final IntConsumer set, final int fallback) {
            super(host, title, description);
            this.get = get;
            this.set = set;
            this.fallback = fallback;
            this.buffer = String.valueOf(get.getAsInt());
        }

        @Override
        int controlWidth(final int available) {
            return WIDTH;
        }

        @Override
        boolean changed() {
            return get.getAsInt() != fallback;
        }

        @Override
        void reset() {
            set.accept(fallback);
            buffer = String.valueOf(fallback);
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
            final boolean focused = host.focused(this);
            if (!focused)
                buffer = String.valueOf(get.getAsInt());
            final int fx = right() - WIDTH;
            final int fy = y + PAD_Y - 3;
            final boolean hit = active() && pointer && MenuStyle.inside(mx, my, fx, fy, WIDTH, HEIGHT);
            field(g, host.font(), this, focused, hit, fx, fy, WIDTH, HEIGHT, 4, null, 0);
        }

        @Override
        boolean clicked(final double mx, final double my, final int button) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT || !MenuStyle.inside(mx, my, right() - WIDTH, y + PAD_Y - 3, WIDTH, HEIGHT))
                return false;
            host.focus(this);
            return true;
        }

        @Override
        public String text() {
            return buffer;
        }

        @Override
        public void text(final String value) {
            final String digits = value.length() > MAX_DIGITS ? value.substring(0, MAX_DIGITS) : value;
            buffer = digits;
            if (!digits.isEmpty())
                set.accept((int) Math.min(Long.parseLong(digits), Integer.MAX_VALUE));
        }

        @Override
        public boolean accepts(final String typed) {
            return typed.chars().allMatch(c -> c >= '0' && c <= '9');
        }

        @Override
        public void submit() {
            host.focus(null);
        }
    }

    static final class StringList extends MenuRow implements TextInput {
        private static final int ENTRY_H = 16;
        private final Supplier<List<String>> get;
        private final Consumer<List<String>> set;
        private final List<String> fallback;
        private final Component placeholder;
        private String pending = "";
        private boolean invalid;

        StringList(final Host host, final Component title, final Component description, final Supplier<List<String>> get, final Consumer<List<String>> set, final List<String> fallback,
                   final Component placeholder) {
            super(host, title, description);
            this.get = get;
            this.set = set;
            this.fallback = new ArrayList<>(fallback);
            this.placeholder = placeholder;
        }

        @Override
        int controlBelow() {
            return (get.get().size() + 1) * ENTRY_H;
        }

        @Override
        boolean changed() {
            return !get.get().equals(fallback);
        }

        @Override
        void reset() {
            set.accept(new ArrayList<>(fallback));
        }

        private int firstY() {
            return y + h - PAD_Y - controlBelow();
        }

        private int buttonX() {
            return right() - ENTRY_H;
        }

        @Override
        void draw(final GuiGraphicsExtractor g, final int mx, final int my, final boolean pointer, final boolean over, final float dt) {
            final Font font = host.font();
            final boolean on = active();
            final List<String> entries = get.get();
            final int fx = left();
            final int fw = buttonX() - fx - 1;
            int ry = firstY();
            for (final String entry : entries) {
                MenuStyle.well(g, fx, ry, fw, ENTRY_H - 1, MenuStyle.WELL);
                g.text(font, trim(font, Component.literal(entry), fw - 8), fx + 4, ry + 3, on ? MenuStyle.TEXT_SOFT : MenuStyle.TEXT_OFF, false);
                final boolean hit = on && pointer && MenuStyle.inside(mx, my, buttonX(), ry, ENTRY_H, ENTRY_H - 1);
                MenuStyle.icon(g, MenuStyle.ICON_CLOSE, buttonX(), ry - 1, hit ? MenuStyle.TEXT : MenuStyle.TEXT_GROUP);
                if (hit)
                    g.requestCursor(CursorTypes.POINTING_HAND);
                ry += ENTRY_H;
            }
            final boolean fieldHit = on && pointer && MenuStyle.inside(mx, my, fx, ry, fw, ENTRY_H - 1);
            field(g, font, this, host.focused(this), fieldHit, fx, ry, fw, ENTRY_H - 1, 4, placeholder, invalid ? MenuStyle.INVALID : 0);
            final boolean addHit = on && pointer && MenuStyle.inside(mx, my, buttonX(), ry, ENTRY_H, ENTRY_H - 1);
            MenuStyle.icon(g, MenuStyle.ICON_PLUS, buttonX(), ry - 1, addHit ? MenuStyle.TEXT : MenuStyle.ACCENT_LIGHT);
            if (addHit)
                g.requestCursor(CursorTypes.POINTING_HAND);
        }

        @Override
        boolean clicked(final double mx, final double my, final int button) {
            if (button != InputConstants.MOUSE_BUTTON_LEFT)
                return false;
            final List<String> entries = get.get();
            int ry = firstY();
            for (int i = 0; i < entries.size(); i++) {
                if (MenuStyle.inside(mx, my, buttonX(), ry, ENTRY_H, ENTRY_H - 1)) {
                    final List<String> next = new ArrayList<>(entries);
                    next.remove(i);
                    set.accept(next);
                    host.click();
                    host.relayout();
                    return true;
                }
                ry += ENTRY_H;
            }
            if (MenuStyle.inside(mx, my, left(), ry, buttonX() - left() - 1, ENTRY_H - 1)) {
                host.focus(this);
                return true;
            }
            if (MenuStyle.inside(mx, my, buttonX(), ry, ENTRY_H, ENTRY_H - 1)) {
                submit();
                return true;
            }
            return false;
        }

        @Override
        public String text() {
            return pending;
        }

        @Override
        public void text(final String value) {
            pending = value.toLowerCase(Locale.ROOT);
            invalid = false;
        }

        @Override
        public boolean accepts(final String typed) {
            return typed.chars().allMatch(c -> c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '/' || c == ':');
        }

        @Override
        public void submit() {
            final String value = pending.trim();
            if (value.isEmpty()) {
                host.focus(null);
                return;
            }
            if (!value.contains(":") || Identifier.tryParse(value) == null || get.get().contains(value)) {
                invalid = true;
                return;
            }
            final List<String> next = new ArrayList<>(get.get());
            next.add(value);
            set.accept(next);
            pending = "";
            host.click();
            host.relayout();
        }
    }
}
