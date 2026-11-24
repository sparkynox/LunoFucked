// Client init — keybind G on title screen triggers bot loop
package com.sparkynox.lunofucked;

import com.sparkynox.lunofucked.core.BotLoop;
import com.sparkynox.lunofucked.gui.NamePoolScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class LunoFuckedClient implements ClientModInitializer {

    public static KeyBinding startKey;
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        // G  — start/stop bot loop
        startKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lunofucked.start",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.lunofucked"
        ));

        // M  — open name pool / config menu
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.lunofucked.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.lunofucked"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (startKey.wasPressed()) {
                if (BotLoop.isRunning()) {
                    BotLoop.stop();
                } else {
                    BotLoop.start(client);
                }
            }
            while (menuKey.wasPressed()) {
                if (client.currentScreen != null || client.player == null) {
                    // open from title or in-game
                    MinecraftClient.getInstance().setScreen(new NamePoolScreen(client.currentScreen));
                }
            }
        });
    }
}
