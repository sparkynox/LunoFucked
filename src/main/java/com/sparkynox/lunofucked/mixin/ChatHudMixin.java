// Watches incoming chat for kick/disconnect indicators (backup to onTick check)
package com.sparkynox.lunofucked.mixin;

import com.sparkynox.lunofucked.LunoFucked;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onAddMessage(Text message, CallbackInfo ci) {
        String msg = message.getString().toLowerCase();
        // log kick-related messages for debug visibility
        if (msg.contains("kicked") || msg.contains("banned") || msg.contains("disconnect")) {
            LunoFucked.LOGGER.info("[LunoFucked] server message: {}", message.getString());
        }
    }
}
