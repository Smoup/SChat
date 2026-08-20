package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatTabs;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=26.1 {
/*import net.minecraft.client.multiplayer.chat.GuiMessageSource;
*///?}

@Mixin(ChatComponent.class)
public abstract class ChatComponentMessageMixin {

    //? if >=26.1 {
    /*@Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void schat$route(Component message, MessageSignature signature, GuiMessageSource source,
                             GuiMessageTag tag, CallbackInfo ci) {
        if (ChatTabs.rebuilding() || !ChatTabs.isMainComponent(this)) {
            return;
        }
        ChatTabs.onMessage(message, signature, source, tag);
        ci.cancel();
    }
    *///?} else {
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true)
    private void schat$route(Component message, MessageSignature signature, GuiMessageTag tag,
                             CallbackInfo ci) {
        if (ChatTabs.rebuilding() || !ChatTabs.isMainComponent(this)) {
            return;
        }
        ChatTabs.onMessage(message, signature, null, tag);
        ci.cancel();
    }
    //?}

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void schat$clear(boolean clearHistory, CallbackInfo ci) {
        if (!ChatTabs.rebuilding() && ChatTabs.isMainComponent(this)) {
            ChatTabs.clearHistory();
        }
    }

    //? if >=1.20.5 {
    @Inject(method = "storeState", at = @At("HEAD"))
    private void schat$storeState(CallbackInfoReturnable<ChatComponent.State> cir) {
        if (ChatTabs.isMainComponent(this)) {
            ChatTabs.storeSnapshot();
        }
    }

    @Inject(method = "restoreState", at = @At("RETURN"))
    private void schat$restoreState(ChatComponent.State state, CallbackInfo ci) {
        if (ChatTabs.isMainComponent(this)) {
            ChatTabs.restoreSnapshot();
        }
    }
    //?}
}
