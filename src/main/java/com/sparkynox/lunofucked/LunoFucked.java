// Mod: Luno Fucked | com.sparkynox.lunofucked | Fabric 1.21.1 | Java 21
package com.sparkynox.lunofucked;

import com.sparkynox.lunofucked.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LunoFucked implements ModInitializer {
    public static final String MOD_ID = "lunofucked";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        LOGGER.info("[LunoFucked] loaded — name pool size: {}", ModConfig.namePool.size());
    }
}
