/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.sndctrl.audio;

import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.ListenerTransform;
import dynamiclabs.immersivefx.lib.logging.IModLog;
import dynamiclabs.immersivefx.sndctrl.SoundControl;
import dynamiclabs.immersivefx.sndctrl.config.Config;
import dynamiclabs.immersivefx.sndctrl.misc.IPanningChannel;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.SOFTDeferredUpdates;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.openal.SOFTOutputMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class NaturalPanning {
    private static final IModLog LOGGER = SoundControl.LOGGER.createChild(NaturalPanning.class);

    private static final double AMBIENCE_SHARE = 0.5D;
    private static final double MOVE_THRESHOLD = 0.05D;
    private static final double TURN_THRESHOLD = Math.cos(Math.toRadians(0.5D));
    private static final long MIN_INTERVAL_NANOS = 8_000_000L;
    private static final double MIN_COMP = 0.5D;
    private static final double MAX_COMP = 1.6D;

    private static final double SIDE_COEFF = 0.288675135D * Math.sqrt(3D);
    private static final double FRONT_COEFF = 0.0552305643D * Math.sqrt(3D);

    private static final Set<Channel> CHANNELS = ConcurrentHashMap.newKeySet();

    private static volatile boolean outputSupported = false;
    private static volatile boolean deferredUpdates = false;
    private static volatile String outputName = "unknown";

    @Nullable
    private static volatile Frame current = null;
    @Nullable
    private static Frame lastApplied = null;
    private static long lastApplyNanos = 0L;

    private static int softnessPercent = -1;
    private static double kPositional = 1D;
    private static double kAmbience = 1D;

    private NaturalPanning() {
    }

    public static final class State {
        double x, y, z;
        boolean hasPos;
        byte relative = -1;
        boolean attenuated = true;
        byte mono = -1;
        float baseVolume = 1F;
        float comp = 1F;
        float vx, vy, vz;
        boolean written;
    }

    public static void onDeviceReady(final long device) {
        CHANNELS.clear();
        lastApplied = null;
        current = null;
        boolean supported = false;
        String name;
        try {
            final ALCCapabilities caps = ALC.createCapabilities(device);
            if (caps.ALC_SOFT_output_mode) {
                final int mode = ALC10.alcGetInteger(device, SOFTOutputMode.ALC_OUTPUT_MODE_SOFT);
                supported = mode == SOFTOutputMode.ALC_STEREO_BASIC_SOFT;
                name = modeName(mode);
            } else if (caps.ALC_SOFT_HRTF && ALC10.alcGetInteger(device, SOFTHRTF.ALC_HRTF_SOFT) == ALC10.ALC_TRUE) {
                name = "HRTF (no output mode extension)";
            } else {
                supported = true;
                name = "unknown (assumed stereo)";
            }
        } catch (@Nonnull final Throwable t) {
            name = "error: " + t.getMessage();
        }
        try {
            deferredUpdates = AL.getCapabilities().AL_SOFT_deferred_updates;
        } catch (@Nonnull final Throwable t) {
            deferredUpdates = false;
        }
        outputSupported = supported;
        outputName = name;
        softnessPercent = -1;
        updateStrength();
        LOGGER.info("3D Panning Softness: output mode %s, %s", name, supported ? "active" : "not used (output pans naturally)");
    }

    @Nonnull
    private static String modeName(final int mode) {
        return switch (mode) {
            case SOFTOutputMode.ALC_STEREO_BASIC_SOFT -> "stereo (basic panning)";
            case SOFTOutputMode.ALC_STEREO_HRTF_SOFT -> "stereo HRTF";
            case SOFTOutputMode.ALC_STEREO_UHJ_SOFT -> "stereo UHJ";
            case SOFTOutputMode.ALC_STEREO_SOFT -> "stereo";
            case SOFTOutputMode.ALC_MONO_SOFT -> "mono";
            case SOFTOutputMode.ALC_QUAD_SOFT -> "quad";
            case SOFTOutputMode.ALC_SURROUND_5_1_SOFT -> "5.1";
            case SOFTOutputMode.ALC_SURROUND_6_1_SOFT -> "6.1";
            case SOFTOutputMode.ALC_SURROUND_7_1_SOFT -> "7.1";
            default -> "0x" + Integer.toHexString(mode);
        };
    }

    @Nonnull
    public static String getOutputName() {
        return outputName;
    }

    static double lateralScale(final double farShare) {
        if (farShare <= 0D)
            return 1D;
        final double q = Math.sqrt(Math.min(farShare, 0.5D) / (1D - Math.min(farShare, 0.5D)));
        return (1D - q) / (1D + q);
    }

    private static int readSoftness() {
        try {
            return Config.CLIENT.sound.panningSoftness.get();
        } catch (@Nonnull final Throwable t) {
            return 30;
        }
    }

    private static boolean updateStrength() {
        final int soft = outputSupported ? Math.max(0, Math.min(100, readSoftness())) : 0;
        if (soft == softnessPercent)
            return false;
        softnessPercent = soft;
        final double s = soft / 100D;
        kPositional = lateralScale(0.5D * s);
        kAmbience = lateralScale(0.5D * s * AMBIENCE_SHARE);
        return true;
    }

    private record Frame(double px, double py, double pz,
                         double rx, double ry, double rz,
                         double ux, double uy, double uz,
                         double fx, double fy, double fz) {

        @Nonnull
        static Frame of(@Nonnull final ListenerTransform t) {
            final Vec3 p = t.position();
            final Vec3 f = t.forward().normalize();
            final Vec3 u = t.up().normalize();
            final Vec3 r = f.cross(u).normalize();
            return new Frame(p.x, p.y, p.z, r.x, r.y, r.z, u.x, u.y, u.z, f.x, f.y, f.z);
        }

        boolean closeTo(@Nonnull final Frame o) {
            final double dx = this.px - o.px, dy = this.py - o.py, dz = this.pz - o.pz;
            if (dx * dx + dy * dy + dz * dz > MOVE_THRESHOLD * MOVE_THRESHOLD)
                return false;
            if (this.fx * o.fx + this.fy * o.fy + this.fz * o.fz < TURN_THRESHOLD)
                return false;
            return this.ux * o.ux + this.uy * o.uy + this.uz * o.uz >= TURN_THRESHOLD;
        }
    }

    public static void onListener(@Nonnull final ListenerTransform transform) {
        final Frame frame = Frame.of(transform);
        current = frame;

        final boolean strengthChanged = updateStrength();
        if (!strengthChanged && softnessPercent <= 0)
            return;
        final Frame last = lastApplied;
        if (!strengthChanged) {
            if (last != null && frame.closeTo(last))
                return;
            final long now = System.nanoTime();
            if (now - lastApplyNanos < MIN_INTERVAL_NANOS)
                return;
        }

        lastApplied = frame;
        lastApplyNanos = System.nanoTime();
        if (CHANNELS.isEmpty())
            return;

        final boolean defer = deferredUpdates;
        if (defer)
            SOFTDeferredUpdates.alDeferUpdatesSOFT();
        try {
            for (final Channel channel : CHANNELS)
                apply((IPanningChannel) channel, false);
        } finally {
            if (defer)
                SOFTDeferredUpdates.alProcessUpdatesSOFT();
        }
    }

    @Nonnull
    public static Vec3 onSetPosition(@Nonnull final Channel channel, @Nonnull final Vec3 pos) {
        final IPanningChannel pc = (IPanningChannel) channel;
        final State st = pc.sndctrl_panState();
        st.x = pos.x;
        st.y = pos.y;
        st.z = pos.z;
        if (!st.hasPos) {
            st.hasPos = true;
            CHANNELS.add(channel);
        }
        if (current == null)
            return pos;
        updateStrengthIfIdle();
        final double[] v = compute(st);
        if (v == null) {
            st.written = false;
            updateGain(pc, st, 1F);
            return pos;
        }
        st.vx = (float) v[0];
        st.vy = (float) v[1];
        st.vz = (float) v[2];
        st.written = true;
        updateGain(pc, st, (float) v[3]);
        return new Vec3(v[0], v[1], v[2]);
    }

    public static float onSetVolume(@Nonnull final Channel channel, final float volume) {
        final State st = ((IPanningChannel) channel).sndctrl_panState();
        st.baseVolume = volume;
        return volume * st.comp;
    }

    public static void onSetRelative(@Nonnull final Channel channel, final boolean relative) {
        final State st = ((IPanningChannel) channel).sndctrl_panState();
        st.relative = (byte) (relative ? 1 : 0);
        apply((IPanningChannel) channel, true);
    }

    public static void onAttenuation(@Nonnull final Channel channel, final boolean attenuated) {
        ((IPanningChannel) channel).sndctrl_panState().attenuated = attenuated;
    }

    public static void onFormat(@Nonnull final Channel channel, final int channels) {
        final State st = ((IPanningChannel) channel).sndctrl_panState();
        st.mono = (byte) (channels == 1 ? 1 : 0);
        apply((IPanningChannel) channel, true);
    }

    public static void onStaticBufferAttached(@Nonnull final Channel channel) {
        final int source = ((IPanningChannel) channel).sndctrl_panSource();
        final int buffer = AL10.alGetSourcei(source, AL10.AL_BUFFER);
        if (buffer != 0)
            onFormat(channel, AL10.alGetBufferi(buffer, AL10.AL_CHANNELS));
    }

    public static void onDestroy(@Nonnull final Channel channel) {
        CHANNELS.remove(channel);
    }

    private static void updateStrengthIfIdle() {
        if (softnessPercent < 0)
            updateStrength();
    }

    private static void apply(@Nonnull final IPanningChannel pc, final boolean force) {
        final State st = pc.sndctrl_panState();
        if (!st.hasPos)
            return;
        final int source = pc.sndctrl_panSource();
        final double[] v = current == null ? null : compute(st);
        if (v == null) {
            if (st.written) {
                AL10.alSource3f(source, AL10.AL_POSITION, (float) st.x, (float) st.y, (float) st.z);
                st.written = false;
            }
            updateGain(pc, st, 1F);
            return;
        }

        final float vx = (float) v[0], vy = (float) v[1], vz = (float) v[2];
        boolean write = force || !st.written;
        if (!write) {
            final double dx = vx - st.vx, dy = vy - st.vy, dz = vz - st.vz;
            final double tol = Math.max(1.0E-4D, 1.0E-3D * v[4]);
            write = dx * dx + dy * dy + dz * dz > tol * tol;
        }
        if (write) {
            AL10.alSource3f(source, AL10.AL_POSITION, vx, vy, vz);
            st.vx = vx;
            st.vy = vy;
            st.vz = vz;
            st.written = true;
        }
        updateGain(pc, st, (float) v[3]);
    }

    private static void updateGain(@Nonnull final IPanningChannel pc, @Nonnull final State st, final float comp) {
        if (Math.abs(comp - st.comp) < 0.002F)
            return;
        st.comp = comp;
        final int source = pc.sndctrl_panSource();
        AL10.alSourcef(source, AL10.AL_MAX_GAIN, comp);
        AL10.alSourcef(source, AL10.AL_GAIN, st.baseVolume * comp);
    }

    @Nullable
    private static double[] compute(@Nonnull final State st) {
        final Frame fr = current;
        if (fr == null || st.relative != 0 || !outputSupported)
            return null;
        final double k = st.attenuated ? kPositional : kAmbience;
        if (k >= 0.9999D)
            return null;

        final double dx = st.x - fr.px, dy = st.y - fr.py, dz = st.z - fr.pz;
        final double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0E-3D)
            return null;

        final double r = dx * fr.rx + dy * fr.ry + dz * fr.rz;
        final double u = dx * fr.ux + dy * fr.uy + dz * fr.uz;
        final double f = dx * fr.fx + dy * fr.fy + dz * fr.fz;

        final double vr = k * r;
        final double vu = u + Math.sqrt(1D - k * k) * Math.abs(r);
        final double vf = f;
        final double len = Math.sqrt(vr * vr + vu * vu + vf * vf);
        if (len < 1.0E-9D)
            return null;
        final double s = dist / len;
        final double lr = vr * s, lu = vu * s, lf = vf * s;

        final double x = fr.px + lr * fr.rx + lu * fr.ux + lf * fr.fx;
        final double y = fr.py + lr * fr.ry + lu * fr.uy + lf * fr.fy;
        final double z = fr.pz + lr * fr.rz + lu * fr.uz + lf * fr.fz;

        double comp = 1D;
        if (st.mono == 1) {
            final double before = stereoPower(r, u, f);
            final double after = stereoPower(lr, lu, lf);
            if (after > 1.0E-6D)
                comp = Math.max(MIN_COMP, Math.min(MAX_COMP, Math.sqrt(before / after)));
        }
        return new double[]{x, y, z, comp, dist};
    }

    static double stereoPower(final double right, final double up, final double forward) {
        final double n = Math.sqrt(right * right + up * up + forward * forward);
        if (n < 1.0E-9D)
            return 1D;
        double x = right / n;
        double z = -forward / n;
        if (z < 0D) {
            final double len2d = Math.sqrt(x * x + z * z);
            double zz = -z / len2d;
            if (zz > 0.5D) {
                double xx = Math.copySign(Math.sqrt((1D - zz) * 0.5D), x / len2d);
                zz = Math.sqrt((1D + zz) * 0.5D);
                xx = xx * 3D - xx * xx * xx * 4D;
                zz = zz * zz * zz * 4D - zz * 3D;
                x = xx * len2d;
                z = -zz * len2d;
            } else {
                x = Math.copySign(len2d, x);
                z = 0D;
            }
        }
        final double front = FRONT_COEFF * -z;
        final double gl = Math.max(0D, 0.5D - SIDE_COEFF * x + front);
        final double gr = Math.max(0D, 0.5D + SIDE_COEFF * x + front);
        return gl * gl + gr * gr;
    }
}
