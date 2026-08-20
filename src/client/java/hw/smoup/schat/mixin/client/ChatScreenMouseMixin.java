package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.Compat;
import hw.smoup.schat.client.chat.ChatLinks;
import hw.smoup.schat.client.chat.ChatOverlay;
import hw.smoup.schat.client.chat.TabStrip;
import hw.smoup.schat.client.config.ChatPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
//? if >=1.21.11 {
import org.spongepowered.asm.mixin.gen.Invoker;
//?}

@Mixin(ChatScreen.class)
public abstract class ChatScreenMouseMixin {

    //? if >=1.21.11 {
    @Invoker("insertionClickMode")
    abstract boolean schat$insertionClickMode();

    @Invoker("handleComponentClicked")
    abstract boolean schat$handleComponentClicked(Style style, boolean insertion);
    //?}

    //? if >=1.21.9 {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void schat$click(MouseButtonEvent event, boolean doubleClick,
                             CallbackInfoReturnable<Boolean> cir) {
        if (schat$handle(event.x(), event.y(), event.button())) {
            cir.setReturnValue(true);
        }
    }
    //?} else {
    /*@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void schat$click(double mouseX, double mouseY, int button,
                             CallbackInfoReturnable<Boolean> cir) {
        if (schat$handle(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
    *///?}

    @Unique
    private boolean schat$handle(double mouseX, double mouseY, int button) {
        if (TabStrip.click(mouseX, mouseY, button) || ChatOverlay.grabsClick(mouseX, mouseY)) {
            return true;
        }
        if (button != 0) {
            return false;
        }
        //? if >=1.21.11 {
        boolean insertion = schat$insertionClickMode();
        Style style = ChatLinks.styleAt(Minecraft.getInstance().font, mouseX, mouseY, insertion);
        return style != null && schat$handleComponentClicked(style, insertion);
        //?} else {
        /*Style style = ChatLinks.styleAt(Minecraft.getInstance().font, mouseX, mouseY, false);
        return style != null && ((ChatScreen) (Object) this).handleComponentClicked(style);
        *///?}
    }

    //? if >=1.20.2 {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void schat$scroll(double mouseX, double mouseY, double scrollX, double scrollY,
                              CallbackInfoReturnable<Boolean> cir) {
        if (schat$scrollPanel(mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
        }
    }
    //?} else {
    /*@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void schat$scroll(double mouseX, double mouseY, double amount,
                              CallbackInfoReturnable<Boolean> cir) {
        if (schat$scrollPanel(mouseX, mouseY, amount)) {
            cir.setReturnValue(true);
        }
    }
    *///?}

    @Unique
    private boolean schat$scrollPanel(double mouseX, double mouseY, double amount) {
        ChatPanel panel = TabStrip.panelAt(mouseX, mouseY);
        if (panel == null) {
            return false;
        }
        ChatComponent component = panel.component();
        if (component == null) {
            return false;
        }
        double lines = Mth.clamp(amount, -1.0, 1.0);
        if (!Compat.shiftDown()) {
            lines *= 7.0;
        }
        component.scrollChat((int) lines);
        return true;
    }
}
