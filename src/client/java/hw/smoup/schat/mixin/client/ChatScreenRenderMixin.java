package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatOverlay;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}

@Mixin(ChatScreen.class)
public abstract class ChatScreenRenderMixin {

    //? if >=26.1 {
    /*@Inject(method = "extractRenderState", at = @At("RETURN"))
    private void schat$overlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ChatOverlay.render(graphics::fill, graphics::text, mouseX, mouseY);
    }
    *///?} else {
    @Inject(method = "render", at = @At("RETURN"))
    private void schat$overlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ChatOverlay.render(graphics::fill, graphics::drawString, mouseX, mouseY);
    }
    //?}
}
