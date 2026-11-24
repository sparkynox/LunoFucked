package com.sparkynox.lunofucked.mixin;

import com.sparkynox.lunofucked.core.BotLoop;
import com.sparkynox.lunofucked.gui.NamePoolScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        addDrawableChild(ButtonWidget.builder(
                Text.literal(BotLoop.isRunning() ? "§c[Luno] STOP" : "§a[Luno] START"),
                btn -> {
                    if (BotLoop.isRunning()) {
                        BotLoop.stop();
                        btn.setMessage(Text.literal("§a[Luno] START"));
                    } else {
                        BotLoop.start(mc);
                        btn.setMessage(Text.literal("§c[Luno] STOP"));
                    }
                })
                .dimensions(width - 110, 4, 106, 18)
                .build()
        );

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§e[Luno] MENU"),
                btn -> mc.setScreen(new NamePoolScreen((TitleScreen)(Object)this)))
                .dimensions(width - 110, 25, 106, 18)
                .build()
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderStatusBar(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        String status = BotLoop.isRunning()
                ? "§a[LunoFucked] §fRunning §8| §7User: §b" + BotLoop.currentName
                  + " §8| §7State: §e" + BotLoop.getState().name()
                : "§c[LunoFucked] §7Idle §8— click §f[Luno] START §7or §fM §7for menu";

        int tw = tr.getWidth(status);
        ctx.fill(0, height - 18, width, height, 0xAA000000);
        ctx.drawTextWithShadow(tr, Text.literal(status), (width - tw) / 2, height - 14, 0xFFFFFF);
    }
}
