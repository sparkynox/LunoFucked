// SessionSwapper — swap MinecraftClient session via type-scan reflection
// Works in both dev (yarn names) and prod (intermediary names) jars
package com.sparkynox.lunofucked.core;

import com.sparkynox.lunofucked.LunoFucked;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

public class SessionSwapper {

    private static Session originalSession = null;
    private static Field   sessionField    = null;

    static {
        // Scan by type — immune to name remapping between dev/prod jars
        for (Field f : MinecraftClient.class.getDeclaredFields()) {
            if (f.getType().equals(Session.class)) {
                f.setAccessible(true);
                sessionField = f;
                LunoFucked.LOGGER.info("[SessionSwapper] found session field: {}", f.getName());
                break;
            }
        }
        if (sessionField == null) {
            LunoFucked.LOGGER.error("[SessionSwapper] Session field not found in MinecraftClient!");
        }
    }

    public static boolean swapTo(String username) {
        if (sessionField == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        try {
            // save original once
            if (originalSession == null) {
                originalSession = (Session) sessionField.get(mc);
                LunoFucked.LOGGER.info("[SessionSwapper] saved original session: {}",
                        originalSession.getUsername());
            }

            // offline UUID — same derivation as vanilla offline mode
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());

            // Session(username, uuid, accessToken, xuid, clientId, accountType)
            // accessToken "0" = cracked, MOJANG type = no MS verify
            Session fake = new Session(
                    username,
                    uuid,
                    "0",
                    Optional.empty(),
                    Optional.empty(),
                    Session.AccountType.MOJANG
            );

            sessionField.set(mc, fake);
            LunoFucked.LOGGER.info("[SessionSwapper] swapped → {} ({})", username, uuid);
            return true;

        } catch (Exception e) {
            LunoFucked.LOGGER.error("[SessionSwapper] swap failed", e);
            return false;
        }
    }

    public static void restore() {
        if (sessionField == null || originalSession == null) return;
        try {
            sessionField.set(MinecraftClient.getInstance(), originalSession);
            LunoFucked.LOGGER.info("[SessionSwapper] restored → {}", originalSession.getUsername());
            originalSession = null;
        } catch (Exception e) {
            LunoFucked.LOGGER.error("[SessionSwapper] restore failed", e);
        }
    }
}
