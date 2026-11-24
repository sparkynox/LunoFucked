// BotLoop — cracked session griefer loop
// Target: Fabric 1.21.4 | Java 21
package com.sparkynox.lunofucked.core;

import com.sparkynox.lunofucked.LunoFucked;
import com.sparkynox.lunofucked.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BotLoop {

    public enum State {
        IDLE, CONNECTING, JOINING, LOGIN_SCREEN, LOGGED_IN,
        REDEEMING, TPA, TRACKING, DROPPING, LEAVING
    }

    private static volatile State   state       = State.IDLE;
    private static volatile boolean running     = false;
    public  static volatile String  currentName = "—";

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "LunoFucked-Scheduler");
                t.setDaemon(true);
                return t;
            });

    private static ScheduledFuture<?> pendingTask;
    private static int ticksSinceConnect = 0;
    private static int ticksSinceLogin   = 0;
    private static MinecraftClient mc;
    private static boolean hooksRegistered = false;

    // ── public API ─────────────────────────────────────────────────────────────

    public static void start(MinecraftClient client) {
        mc = client;
        running = true;
        ModConfig.resetNameCycle();

        if (!hooksRegistered) {
            ClientTickEvents.END_CLIENT_TICK.register(BotLoop::onTick);

            // reliable disconnect detection
            ClientPlayConnectionEvents.DISCONNECT.register((handler, c) -> {
                if (!running) return;
                LunoFucked.LOGGER.info("[LunoFucked] disconnected (event) — reconnecting in 3s");
                cancelPending();
                state = State.IDLE;
                LoginScreenHandler.reset();
                scheduleConnect(3000);
            });

            hooksRegistered = true;
        }

        LunoFucked.LOGGER.info("[LunoFucked] bot started");
        scheduleConnect(0);
    }

    public static void stop() {
        running     = false;
        state       = State.IDLE;
        currentName = "—";
        cancelPending();
        LoginScreenHandler.reset();
        SessionSwapper.restore();
        LunoFucked.LOGGER.info("[LunoFucked] bot stopped");
    }

    public static boolean isRunning() { return running; }
    public static State   getState()  { return state;   }

    // ── connect ────────────────────────────────────────────────────────────────

    private static void scheduleConnect(long delayMs) {
        if (!running) return;
        pendingTask = SCHEDULER.schedule(() -> mc.execute(() -> {
            currentName       = ModConfig.nextName();
            ticksSinceConnect = 0;
            ticksSinceLogin   = 0;
            LoginScreenHandler.reset();

            // swap session BEFORE connecting — this sets the in-game name
            boolean swapped = SessionSwapper.swapTo(currentName);
            if (!swapped) {
                LunoFucked.LOGGER.warn("[LunoFucked] session swap failed — connecting anyway");
            }

            state = State.CONNECTING;
            LunoFucked.LOGGER.info("[LunoFucked] connecting as {}", currentName);
            connectToServer();
        }), delayMs, TimeUnit.MILLISECONDS);
    }

    private static void connectToServer() {
        ServerInfo info = new ServerInfo(currentName, ModConfig.serverIp, ServerInfo.ServerType.OTHER);
        ConnectScreen.connect(
                new TitleScreen(),
                mc,
                ServerAddress.parse(ModConfig.serverIp),
                info,
                false,
                (CookieStorage) null
        );
        state = State.JOINING;
    }

    // ── tick ───────────────────────────────────────────────────────────────────

    private static void onTick(MinecraftClient client) {
        if (!running) return;
        ClientPlayerEntity player = client.player;

        switch (state) {

            case JOINING -> {
                ticksSinceConnect++;
                // login GUI appeared (not title, not connect screen)
                if (client.currentScreen != null
                        && !(client.currentScreen instanceof TitleScreen)
                        && !(client.currentScreen instanceof ConnectScreen)) {
                    state = State.LOGIN_SCREEN;
                    LunoFucked.LOGGER.info("[LunoFucked] login screen detected");
                }
                if (player != null) { state = State.LOGGED_IN; ticksSinceLogin = 0; }
                if (ticksSinceConnect > 400) {
                    // stuck — disconnect event will trigger reconnect
                    var net = client.getNetworkHandler();
                    if (net != null) net.getConnection().disconnect(Text.literal("timeout"));
                }
            }

            case LOGIN_SCREEN -> {
                if (LoginScreenHandler.tryHandle(client)) {
                    state = State.LOGGED_IN;
                    ticksSinceLogin = 0;
                }
                if (player != null) { state = State.LOGGED_IN; ticksSinceLogin = 0; }
            }

            case LOGGED_IN -> {
                ticksSinceLogin++;
                if (player != null && ticksSinceLogin > 60) {
                    state = State.REDEEMING;
                    sendCommand("redeem " + ModConfig.redeemCode);
                    schedule(1000, () -> {
                        if (!running) return;
                        state = State.TPA;
                        sendCommand("tpa " + ModConfig.tpaTarget);
                        schedule(3000, () -> { if (running) state = State.TRACKING; });
                    });
                }
                if (ticksSinceLogin > 400) {
                    var net = client.getNetworkHandler();
                    if (net != null) net.getConnection().disconnect(Text.literal("login timeout"));
                }
            }

            case TRACKING -> {
                if (player == null || client.world == null) return;
                client.world.getPlayers().stream()
                        .filter(e -> !e.isMainPlayer()
                                && e.getName().getString().equalsIgnoreCase(ModConfig.tpaTarget))
                        .findFirst()
                        .ifPresent(target -> {
                            if (player.getPos().distanceTo(target.getPos()) <= ModConfig.dropDistance) {
                                state = State.DROPPING;
                                dropAllItems(player);
                                schedule(500, () -> mc.execute(() -> {
                                    state = State.LEAVING;
                                    var net = client.getNetworkHandler();
                                    if (net != null) net.getConnection().disconnect(
                                            Text.literal("LunoFucked — cycle done"));
                                    // DISCONNECT event fires → scheduleConnect(3000)
                                }));
                            }
                        });
            }

            default -> {}
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private static void sendCommand(String cmd) {
        if (mc == null || mc.player == null) return;
        mc.player.networkHandler.sendChatCommand(cmd);
    }

    private static void dropAllItems(ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty()) { player.dropItem(s, true); inv.setStack(i, ItemStack.EMPTY); }
        }
        LunoFucked.LOGGER.info("[LunoFucked] inventory cleared");
    }

    private static void cancelPending() {
        if (pendingTask != null) pendingTask.cancel(false);
    }

    private static void schedule(long ms, Runnable task) {
        pendingTask = SCHEDULER.schedule(() -> mc.execute(task), ms, TimeUnit.MILLISECONDS);
    }
}
