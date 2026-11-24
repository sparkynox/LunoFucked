// LoginScreenHandler — detects insanesmp custom login GUI and auto-fills password + clicks Login
// Strategy: scan currentScreen's children for TextFieldWidget, fill password, find Login button, click
package com.sparkynox.lunofucked.core;

import com.sparkynox.lunofucked.LunoFucked;
import com.sparkynox.lunofucked.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.List;

public class LoginScreenHandler {

    private static boolean handled = false;
    private static int ticksWaiting = 0;

    /** Call every tick from BotLoop while in JOINING/REGISTERING state */
    public static boolean tryHandle(MinecraftClient mc) {
        Screen screen = mc.currentScreen;
        if (screen == null) { handled = false; ticksWaiting = 0; return false; }
        if (handled) return true;

        ticksWaiting++;
        // give the screen a couple ticks to fully init before we poke it
        if (ticksWaiting < 5) return false;

        List<? extends Element> children = screen.children();
        if (children.isEmpty()) return false;

        // find the password text field — it'll be the only (or first) TextFieldWidget
        TextFieldWidget passField = null;
        ButtonWidget loginButton = null;

        for (Element el : children) {
            if (el instanceof TextFieldWidget tf && passField == null) {
                passField = tf;
            }
            if (el instanceof ButtonWidget btn) {
                String label = btn.getMessage().getString().toLowerCase();
                if (label.contains("login") || label.contains("log in") || label.contains("connect")) {
                    loginButton = btn;
                }
            }
        }

        if (passField == null || loginButton == null) {
            LunoFucked.LOGGER.debug("[LunoFucked] login screen found but widgets not ready yet (tick {})", ticksWaiting);
            // give up after 100 ticks (~5s) and fall back to chat command
            if (ticksWaiting > 100) {
                LunoFucked.LOGGER.warn("[LunoFucked] login screen timeout — falling back to /login command");
                handled = true;
                sendLoginCommand(mc);
            }
            return false;
        }

        // fill password
        passField.setFocused(true);
        passField.setText(ModConfig.loginPassword);
        LunoFucked.LOGGER.info("[LunoFucked] filled login password field");

        // click login button
        final ButtonWidget btn = loginButton;
        btn.onPress();
        LunoFucked.LOGGER.info("[LunoFucked] clicked Login button");

        handled = true;
        return true;
    }

    /** Fallback: send /login via chat command if GUI interaction fails */
    private static void sendLoginCommand(MinecraftClient mc) {
        if (mc.player == null) return;
        mc.player.networkHandler.sendChatCommand("login " + ModConfig.loginPassword);
    }

    /** Reset between sessions */
    public static void reset() {
        handled = false;
        ticksWaiting = 0;
    }
}
