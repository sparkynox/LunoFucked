// Config — persists to .minecraft/config/lunofucked.json
package com.sparkynox.lunofucked.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sparkynox.lunofucked.LunoFucked;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("lunofucked.json");

    // connection
    public static String serverIp        = "insanesmp.net";

    // login GUI password (typed into the insanesmp login screen)
    public static String loginPassword   = "SparkyNoxOP";

    // post-login commands
    public static String redeemCode      = "insane";
    public static String tpaTarget       = "FuckOffAds";

    // behaviour
    public static float  dropDistance    = 3.5f;

    // name pool
    public static List<String> namePool  = new ArrayList<>(List.of(
            "SparkyNox09",
            "YTLEgend69",
            "LunoLoL682"
    ));

    // internal
    private static int nameIndex = 0;

    public static synchronized String nextName() {
        if (namePool.isEmpty()) return "LunoBot" + (++nameIndex);
        String base    = namePool.get(nameIndex % namePool.size());
        int    cycle   = nameIndex / namePool.size();
        String stripped = base.replaceAll("\\d+$", "");
        Matcher m = Pattern.compile("\\d+$").matcher(base);
        String origSuffix = m.find() ? m.group() : "";
        String name = cycle == 0
                ? stripped + origSuffix
                : stripped + (cycle * 100 + (nameIndex % namePool.size()));
        name = name.replaceAll("[^a-zA-Z0-9_]", "_");
        if (name.length() > 16) name = name.substring(0, 16);
        nameIndex++;
        return name;
    }

    public static synchronized void resetNameCycle() { nameIndex = 0; }

    // ── serialisation ──────────────────────────────────────────────────────────

    private static class ConfigData {
        String serverIp, loginPassword, redeemCode, tpaTarget;
        float  dropDistance;
        List<String> namePool;
    }

    public static void load() {
        File f = CONFIG_PATH.toFile();
        if (!f.exists()) { save(); return; }
        try (Reader r = new FileReader(f)) {
            ConfigData d = GSON.fromJson(r, ConfigData.class);
            if (d == null) return;
            if (d.serverIp       != null) serverIp       = d.serverIp;
            if (d.loginPassword  != null) loginPassword  = d.loginPassword;
            if (d.redeemCode     != null) redeemCode     = d.redeemCode;
            if (d.tpaTarget      != null) tpaTarget      = d.tpaTarget;
            if (d.dropDistance   != 0)    dropDistance   = d.dropDistance;
            if (d.namePool != null && !d.namePool.isEmpty()) namePool = d.namePool;
        } catch (IOException e) {
            LunoFucked.LOGGER.error("[LunoFucked] config load failed", e);
        }
    }

    public static void save() {
        ConfigData d  = new ConfigData();
        d.serverIp      = serverIp;
        d.loginPassword = loginPassword;
        d.redeemCode    = redeemCode;
        d.tpaTarget     = tpaTarget;
        d.dropDistance  = dropDistance;
        d.namePool      = namePool;
        try (Writer w = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(d, w);
        } catch (IOException e) {
            LunoFucked.LOGGER.error("[LunoFucked] config save failed", e);
        }
    }
}
