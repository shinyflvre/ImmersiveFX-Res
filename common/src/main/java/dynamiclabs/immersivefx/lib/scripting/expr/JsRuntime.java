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

package dynamiclabs.immersivefx.lib.scripting.expr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JsRuntime {

    private JsRuntime() {
    }

    public static boolean isNullish(@Nullable final Object o) {
        return o == null || o == Undefined.INSTANCE;
    }

    public static boolean isString(@Nullable final Object o) {
        return o instanceof CharSequence || o instanceof Character;
    }

    public static boolean isPrimitive(@Nullable final Object o) {
        return isNullish(o) || o instanceof Number || o instanceof Boolean || isString(o);
    }

    public static boolean toBoolean(@Nullable final Object o) {
        if (o instanceof Boolean)
            return (Boolean) o;
        if (isNullish(o))
            return false;
        if (o instanceof Number) {
            final double d = ((Number) o).doubleValue();
            return d != 0 && !Double.isNaN(d);
        }
        if (isString(o))
            return o.toString().length() > 0;
        return true;
    }

    public static double toNumber(@Nullable final Object o) {
        if (o instanceof Number)
            return ((Number) o).doubleValue();
        if (o instanceof Boolean)
            return (Boolean) o ? 1 : 0;
        if (o == null)
            return 0;
        if (o == Undefined.INSTANCE)
            return Double.NaN;
        final String s = (isString(o) ? o.toString() : toPrimitiveString(o)).trim();
        if (s.isEmpty())
            return 0;
        try {
            if (s.startsWith("0x") || s.startsWith("0X"))
                return Long.parseLong(s.substring(2), 16);
            if ("Infinity".equals(s) || "+Infinity".equals(s))
                return Double.POSITIVE_INFINITY;
            if ("-Infinity".equals(s))
                return Double.NEGATIVE_INFINITY;
            final char last = s.charAt(s.length() - 1);
            if (!Character.isDigit(last) && last != '.')
                return Double.NaN;
            return Double.parseDouble(s);
        } catch (@Nonnull final NumberFormatException ex) {
            return Double.NaN;
        }
    }

    @Nonnull
    public static String toStr(@Nullable final Object o) {
        if (o == null)
            return "null";
        if (o == Undefined.INSTANCE)
            return "undefined";
        if (o instanceof Double || o instanceof Float)
            return numberToString(((Number) o).doubleValue());
        if (o instanceof Number)
            return o.toString();
        if (o instanceof Boolean || isString(o))
            return o.toString();
        return toPrimitiveString(o);
    }

    @Nonnull
    private static String toPrimitiveString(@Nonnull final Object o) {
        return String.valueOf(o);
    }

    @Nonnull
    public static String numberToString(final double d) {
        if (Double.isNaN(d))
            return "NaN";
        if (Double.isInfinite(d))
            return d > 0 ? "Infinity" : "-Infinity";
        if (d == Math.rint(d) && Math.abs(d) < 1e21)
            return Long.toString((long) d);
        String s = Double.toString(d);
        final int e = s.indexOf('E');
        if (e > 0) {
            final String mantissa = s.substring(0, e);
            final String exp = s.substring(e + 1);
            s = (mantissa.endsWith(".0") ? mantissa.substring(0, mantissa.length() - 2) : mantissa)
                    + "e" + (exp.startsWith("-") ? exp : "+" + exp);
        }
        return s;
    }

    @Nullable
    public static Object toJava(@Nullable final Object o) {
        return o == Undefined.INSTANCE ? null : o;
    }

    public static boolean looseEquals(@Nullable final Object a, @Nullable final Object b) {
        if (isNullish(a) || isNullish(b))
            return isNullish(a) && isNullish(b);
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        if (isString(a) && isString(b))
            return a.toString().equals(b.toString());
        if (a instanceof Boolean && b instanceof Boolean)
            return a.equals(b);
        if (a instanceof Boolean)
            return looseEquals(toNumber(a), b);
        if (b instanceof Boolean)
            return looseEquals(a, toNumber(b));
        if (a instanceof Number && isString(b))
            return ((Number) a).doubleValue() == toNumber(b);
        if (isString(a) && b instanceof Number)
            return toNumber(a) == ((Number) b).doubleValue();
        if (isPrimitive(a) != isPrimitive(b)) {
            final Object obj = isPrimitive(a) ? b : a;
            final Object prim = isPrimitive(a) ? a : b;
            if (obj instanceof Enum<?> || !(prim instanceof Number))
                return toStr(obj).equals(toStr(prim));
            return toNumber(toStr(obj)) == ((Number) prim).doubleValue();
        }
        return a == b || a.equals(b);
    }

    public static boolean strictEquals(@Nullable final Object a, @Nullable final Object b) {
        if (a == Undefined.INSTANCE || b == Undefined.INSTANCE)
            return a == b;
        if (a == null || b == null)
            return a == b;
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        if (isString(a) && isString(b))
            return a.toString().equals(b.toString());
        if (a instanceof Boolean && b instanceof Boolean)
            return a.equals(b);
        if (isPrimitive(a) || isPrimitive(b))
            return false;
        return a == b || a.equals(b);
    }

    public static int compare(@Nullable final Object a, @Nullable final Object b) {
        if (isString(a) && isString(b))
            return Integer.signum(a.toString().compareTo(b.toString()));
        final double x = toNumber(a);
        final double y = toNumber(b);
        if (Double.isNaN(x) || Double.isNaN(y))
            return -2;
        return Double.compare(x, y) < 0 ? -1 : (x == y ? 0 : 1);
    }

    @Nonnull
    public static Object add(@Nullable final Object a, @Nullable final Object b) {
        final boolean primA = a instanceof Number || a instanceof Boolean || isNullish(a);
        final boolean primB = b instanceof Number || b instanceof Boolean || isNullish(b);
        if (primA && primB)
            return toNumber(a) + toNumber(b);
        return toStr(a) + toStr(b);
    }

    public static final class BoundMethod {
        final Object target;
        final String name;

        BoundMethod(@Nonnull final Object target, @Nonnull final String name) {
            this.target = target;
            this.name = name;
        }

        @Override
        public String toString() {
            return "function " + this.name + "() { [native code] }";
        }
    }

    private interface PropertyReader {
        Object read(Object target) throws Throwable;
    }

    private static final PropertyReader UNDEFINED_READER = t -> Undefined.INSTANCE;

    private static final Map<Class<?>, Map<String, PropertyReader>> PROPERTY_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, List<Method>>> METHOD_CACHE = new ConcurrentHashMap<>();

    @Nullable
    public static Object getProperty(@Nullable final Object target, @Nonnull final String name) {
        if (isNullish(target))
            throw new ExpressionException("Cannot read property \"" + name + "\" of " + toStr(target));

        if (target instanceof Map<?, ?> map) {
            if (map.containsKey(name))
                return map.get(name);
        }

        if (isString(target) && "length".equals(name))
            return (double) target.toString().length();
        if (target.getClass().isArray() && "length".equals(name))
            return (double) Array.getLength(target);

        final Class<?> clazz = target.getClass();
        final PropertyReader reader = PROPERTY_CACHE
                .computeIfAbsent(clazz, c -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> resolveProperty(clazz, n));
        try {
            return reader.read(target);
        } catch (@Nonnull final InvocationTargetException ex) {
            throw new ExpressionException("Error reading property \"" + name + "\" of " + clazz.getName(), ex.getCause());
        } catch (@Nonnull final Throwable t) {
            throw new ExpressionException("Error reading property \"" + name + "\" of " + clazz.getName(), t);
        }
    }

    @Nullable
    public static Object getIndexed(@Nullable final Object target, @Nullable final Object key) {
        if (isNullish(target))
            throw new ExpressionException("Cannot read property \"" + toStr(key) + "\" of " + toStr(target));
        if (target instanceof List<?> list && key instanceof Number) {
            final int idx = (int) ((Number) key).doubleValue();
            return idx >= 0 && idx < list.size() ? list.get(idx) : Undefined.INSTANCE;
        }
        if (target.getClass().isArray() && key instanceof Number) {
            final int idx = (int) ((Number) key).doubleValue();
            return idx >= 0 && idx < Array.getLength(target) ? Array.get(target, idx) : Undefined.INSTANCE;
        }
        if (isString(target) && key instanceof Number) {
            final String s = target.toString();
            final int idx = (int) ((Number) key).doubleValue();
            return idx >= 0 && idx < s.length() ? String.valueOf(s.charAt(idx)) : Undefined.INSTANCE;
        }
        if (target instanceof Map<?, ?> map) {
            final Object k = isString(key) ? key.toString() : key;
            if (map.containsKey(k))
                return map.get(k);
        }
        return getProperty(target, toStr(key));
    }

    @Nonnull
    private static PropertyReader resolveProperty(@Nonnull final Class<?> clazz, @Nonnull final String name) {
        if (name.isEmpty())
            return UNDEFINED_READER;
        final String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        for (final String getter : new String[]{"get" + cap, "is" + cap}) {
            for (final Method m : getMethods(clazz, getter)) {
                if (m.getParameterCount() == 0 && m.getReturnType() != void.class
                        && (!getter.startsWith("is") || m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                    return t -> m.invoke(t);
                }
            }
        }

        try {
            final Field f = clazz.getField(name);
            if (makeAccessible(f))
                return f::get;
        } catch (@Nonnull final NoSuchFieldException ignore) {
        }

        if (!getMethods(clazz, name).isEmpty())
            return target -> new BoundMethod(target, name);

        return UNDEFINED_READER;
    }

    @Nullable
    public static Object invoke(@Nullable final Object target, @Nonnull final String name, @Nonnull final Object[] args) {
        if (isNullish(target))
            throw new ExpressionException("Cannot call method \"" + name + "\" of " + toStr(target));

        if (target instanceof Map<?, ?> map && map.get(name) instanceof BoundMethod bm)
            return invoke(bm.target, bm.name, args);

        final Class<?> clazz = target.getClass();
        final List<Method> candidates = getMethods(clazz, name);
        if (candidates.isEmpty())
            throw new ExpressionException("Object of class " + clazz.getName() + " has no method \"" + name + "\"");

        Method best = null;
        Object[] bestArgs = null;
        int bestCost = Integer.MAX_VALUE;
        for (final Method m : candidates) {
            final Object[] converted = new Object[m.getParameterCount()];
            final int cost = convertArguments(m, args, converted);
            if (cost >= 0 && cost < bestCost) {
                best = m;
                bestArgs = converted;
                bestCost = cost;
            }
        }

        if (best == null)
            throw new ExpressionException("No overload of method \"" + name + "\" of " + clazz.getName() + " accepts " + args.length + " argument(s)");

        try {
            final Object result = best.invoke(target, bestArgs);
            if (best.getReturnType() == void.class)
                return Undefined.INSTANCE;
            return result;
        } catch (@Nonnull final InvocationTargetException ex) {
            throw new ExpressionException("Error calling method \"" + name + "\" of " + clazz.getName(), ex.getCause());
        } catch (@Nonnull final Throwable t) {
            throw new ExpressionException("Error calling method \"" + name + "\" of " + clazz.getName(), t);
        }
    }

    @Nullable
    public static Object call(@Nullable final Object function, @Nonnull final Object[] args) {
        if (function instanceof BoundMethod bm)
            return invoke(bm.target, bm.name, args);
        throw new ExpressionException(toStr(function) + " is not a function");
    }

    @Nonnull
    private static List<Method> getMethods(@Nonnull final Class<?> clazz, @Nonnull final String name) {
        return METHOD_CACHE.computeIfAbsent(clazz, JsRuntime::indexMethods).getOrDefault(name, Collections.emptyList());
    }

    @Nonnull
    private static Map<String, List<Method>> indexMethods(@Nonnull final Class<?> clazz) {
        final Map<String, List<Method>> result = new ConcurrentHashMap<>();
        for (final Method m : clazz.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getDeclaringClass() == Object.class)
                continue;
            final Method callable = findAccessible(clazz, m);
            if (callable != null)
                result.computeIfAbsent(m.getName(), k -> new ArrayList<>()).add(callable);
        }
        return result;
    }

    @Nullable
    private static Method findAccessible(@Nonnull final Class<?> clazz, @Nonnull final Method m) {
        if (Modifier.isPublic(m.getDeclaringClass().getModifiers()) && makeAccessible(m))
            return m;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            final Method found = findInPublicTypes(c, m);
            if (found != null)
                return found;
        }
        return makeAccessible(m) ? m : null;
    }

    @Nullable
    private static Method findInPublicTypes(@Nonnull final Class<?> c, @Nonnull final Method m) {
        if (Modifier.isPublic(c.getModifiers())) {
            try {
                final Method r = c.getMethod(m.getName(), m.getParameterTypes());
                if (Modifier.isPublic(r.getDeclaringClass().getModifiers()) && makeAccessible(r))
                    return r;
            } catch (@Nonnull final NoSuchMethodException ignore) {
            }
        }
        for (final Class<?> i : c.getInterfaces()) {
            final Method r = findInPublicTypes(i, m);
            if (r != null)
                return r;
        }
        return null;
    }

    private static boolean makeAccessible(@Nonnull final java.lang.reflect.AccessibleObject obj) {
        try {
            return obj.trySetAccessible() || obj.canAccess(null);
        } catch (@Nonnull final Throwable t) {
            return false;
        }
    }

    private static final int NO_MATCH = -1;

    private static int convertArguments(@Nonnull final Method m, @Nonnull final Object[] args, @Nonnull final Object[] out) {
        final Class<?>[] types = m.getParameterTypes();
        final int count = types.length;
        int cost = 0;
        if (m.isVarArgs()) {
            if (args.length < count - 1)
                return NO_MATCH;
            for (int i = 0; i < count - 1; i++) {
                final int c = convert(args[i], types[i], out, i);
                if (c < 0)
                    return NO_MATCH;
                cost += c;
            }
            if (args.length == count && args[count - 1] != null && types[count - 1].isInstance(args[count - 1])) {
                out[count - 1] = args[count - 1];
                return cost;
            }
            final Class<?> component = types[count - 1].getComponentType();
            final int n = args.length - (count - 1);
            final Object array = Array.newInstance(component, n);
            final Object[] tmp = new Object[1];
            for (int i = 0; i < n; i++) {
                final int c = convert(args[count - 1 + i], component, tmp, 0);
                if (c < 0)
                    return NO_MATCH;
                cost += c;
                Array.set(array, i, tmp[0]);
            }
            out[count - 1] = array;
            return cost + 10;
        }

        if (args.length != count)
            return NO_MATCH;
        for (int i = 0; i < count; i++) {
            final int c = convert(args[i], types[i], out, i);
            if (c < 0)
                return NO_MATCH;
            cost += c;
        }
        return cost;
    }

    private static int convert(@Nullable final Object arg, @Nonnull final Class<?> type, @Nonnull final Object[] out, final int idx) {
        final Object value = toJava(arg);

        if (type == boolean.class || type == Boolean.class) {
            out[idx] = toBoolean(arg);
            return value instanceof Boolean ? 0 : 3;
        }
        if (type == double.class || type == Double.class) {
            out[idx] = toNumber(arg);
            return value instanceof Number ? 0 : 3;
        }
        if (type == float.class || type == Float.class) {
            out[idx] = (float) toNumber(arg);
            return value instanceof Number ? 1 : 3;
        }
        if (type == int.class || type == Integer.class) {
            out[idx] = toInt32(toNumber(arg));
            return value instanceof Number ? 2 : 4;
        }
        if (type == long.class || type == Long.class) {
            out[idx] = (long) toNumber(arg);
            return value instanceof Number ? 2 : 4;
        }
        if (type == short.class || type == Short.class) {
            out[idx] = (short) toInt32(toNumber(arg));
            return value instanceof Number ? 3 : 5;
        }
        if (type == byte.class || type == Byte.class) {
            out[idx] = (byte) toInt32(toNumber(arg));
            return value instanceof Number ? 3 : 5;
        }
        if (type == char.class || type == Character.class) {
            final String s = toStr(arg);
            if (s.isEmpty())
                return NO_MATCH;
            out[idx] = s.charAt(0);
            return isString(value) ? 1 : 5;
        }
        if (type == String.class || type == CharSequence.class) {
            out[idx] = value == null ? null : toStr(value);
            return isString(value) || value == null ? 0 : 4;
        }
        if (value == null) {
            if (type.isPrimitive())
                return NO_MATCH;
            out[idx] = null;
            return 1;
        }
        if (type.isInstance(value)) {
            out[idx] = value;
            return type == Object.class ? 2 : 0;
        }
        if (type.isEnum() && isString(value)) {
            for (final Object constant : type.getEnumConstants()) {
                if (((Enum<?>) constant).name().equalsIgnoreCase(value.toString())) {
                    out[idx] = constant;
                    return 2;
                }
            }
            return NO_MATCH;
        }
        return NO_MATCH;
    }

    private static int toInt32(final double d) {
        if (Double.isNaN(d) || Double.isInfinite(d))
            return 0;
        return (int) (long) d;
    }
}
