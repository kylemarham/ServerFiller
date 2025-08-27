package me.seetaadev.serverfiller.bot.cookie;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;

import java.lang.reflect.Constructor;
import java.util.Collections;

public final class BotCookieFactory {
    private BotCookieFactory() {}

    public static CommonListenerCookie make(GameProfile profile, int latency, ClientInformation info) {
        try {
            // Try new (1.21.8) constructor: (GameProfile, int, ClientInformation, boolean, @Nullable String, Set<String>, KeepAlive)
            for (Constructor<?> c : CommonListenerCookie.class.getDeclaredConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 7) {
                    Object keepAlive = resolveKeepAlive();
                    return (CommonListenerCookie) c.newInstance(
                            profile,
                            latency,
                            info,
                            false,                   // transferred
                            null,                    // brandName: null/unknown
                            Collections.<String>emptySet(), // no plugin channels at login
                            keepAlive                // resolved KeepAlive instance
                    );
                }
            }
            // Fallback (1.21.4): (GameProfile, int, ClientInformation, boolean)
            Constructor<CommonListenerCookie> oldCtor =
                    CommonListenerCookie.class.getConstructor(
                            GameProfile.class,
                            int.class,
                            ClientInformation.class,
                            boolean.class
                    );
            return oldCtor.newInstance(profile, latency, info, false);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct CommonListenerCookie reflectively", e);
        }
    }

    private static Object resolveKeepAlive() {
        try {
            Class<?> ka = Class.forName("io.papermc.paper.util.KeepAlive");
            // Most likely options across builds — try a few safe names:
            try { return ka.getField("DEFAULT").get(null); } catch (NoSuchFieldException ignored) {}
            try { return ka.getField("DISABLED").get(null); } catch (NoSuchFieldException ignored) {}
            try { return ka.getMethod("defaultKeepAlive").invoke(null); } catch (NoSuchMethodException ignored) {}
            try { return ka.getMethod("disabled").invoke(null); } catch (NoSuchMethodException ignored) {}
            try { return ka.getConstructor().newInstance(); } catch (NoSuchMethodException ignored) {}
        } catch (Throwable t) {
            // fall-through
        }
        throw new IllegalStateException("Cannot resolve io.papermc.paper.util.KeepAlive on this Paper build");
    }
}
