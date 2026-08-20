package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatOverlay;
import hw.smoup.schat.client.chat.TabStrip;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonInfo;
//?}

// Состояние кнопки берём отсюда, а не из MouseHandler.isLeftPressed: ваниль обновляет
// это поле только когда экран закрыт, а нам нужен зажим при открытом ChatScreen.
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    //? if >=1.21.9 {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void schat$button(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        ChatOverlay.onMouseButton(buttonInfo.button(), action, buttonInfo.modifiers());
        TabStrip.onMouseButton(buttonInfo.button(), action);
    }
    //?} else {
    /*@Inject(method = "onPress", at = @At("HEAD"))
    private void schat$button(long window, int button, int action, int modifiers, CallbackInfo ci) {
        ChatOverlay.onMouseButton(button, action, modifiers);
        TabStrip.onMouseButton(button, action);
    }
    *///?}
}
