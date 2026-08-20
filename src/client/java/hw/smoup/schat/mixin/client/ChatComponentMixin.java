package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatTabs;
import hw.smoup.schat.client.chat.PanelBound;
import hw.smoup.schat.client.config.ChatPanel;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements PanelBound {

    @Shadow
    public abstract boolean isChatFocused();

    @Unique
    private ChatPanel schat$panel;

    @Override
    public ChatPanel schat$panel() {
        return schat$panel;
    }

    @Override
    public void schat$setPanel(ChatPanel panel) {
        schat$panel = panel;
    }

    @Inject(method = "getWidth()I", at = @At("HEAD"), cancellable = true)
    private void schat$width(CallbackInfoReturnable<Integer> cir) {
        ChatPanel panel = schat$boundPanel();
        if (panel != null) {
            cir.setReturnValue(panel.effectiveWidth());
        }
    }

    @Inject(method = "getHeight()I", at = @At("HEAD"), cancellable = true)
    private void schat$height(CallbackInfoReturnable<Integer> cir) {
        ChatPanel panel = schat$boundPanel();
        if (panel != null) {
            cir.setReturnValue(panel.effectiveHeight(isChatFocused()));
        }
    }

    @Inject(method = "getScale()D", at = @At("HEAD"), cancellable = true)
    private void schat$scale(CallbackInfoReturnable<Double> cir) {
        ChatPanel panel = schat$boundPanel();
        if (panel != null) {
            cir.setReturnValue(panel.scale());
        }
    }

    @Unique
    private ChatPanel schat$boundPanel() {
        ChatPanel panel = ChatTabs.panelOf(this);
        return panel == null || panel.empty() ? null : panel;
    }
}
