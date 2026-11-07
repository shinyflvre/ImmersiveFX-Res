/*
 * ImmersiveFX
 * Copyright (C) 2026 Shinyflvres. See LICENSE.
 */
package dynamiclabs.immersivefx.platform;

import java.util.Iterator;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;

public final class Platform {

    private static volatile IPlatform instance;

    private Platform() {
    }

    @Nonnull
    public static IPlatform get() {
        IPlatform result = instance;
        if (result == null) {
            synchronized (Platform.class) {
                result = instance;
                if (result == null)
                    instance = result = load();
            }
        }
        return result;
    }

    @Nonnull
    private static IPlatform load() {
        final Iterator<IPlatform> it = ServiceLoader.load(IPlatform.class, Platform.class.getClassLoader()).iterator();
        if (!it.hasNext())
            throw new IllegalStateException("No ImmersiveFX platform implementation found (META-INF/services/" + IPlatform.class.getName() + ")");
        final IPlatform result = it.next();
        if (it.hasNext())
            throw new IllegalStateException("More than one ImmersiveFX platform implementation found");
        return result;
    }
}
