package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.ChatFrame;
import hw.smoup.schat.client.chat.ChatTabs;
import hw.smoup.schat.client.chat.TabStrip;
import hw.smoup.schat.client.config.ChatPanel;
import hw.smoup.schat.client.config.SchatConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?}
//? if >=1.21.11 {
import net.minecraft.client.gui.Font;
//?}
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}

@Mixin(ChatComponent.class)
public abstract class ChatComponentPositionMixin {

    @Unique
    private static final int TEXT_BACKGROUND_OPACITY_READ = 1;

    @Shadow
    public abstract boolean isChatFocused();

    //? if >=1.21.11 && <26.1 {
    @Inject(method = "render", at = @At("HEAD"))
    private void schat$push(GuiGraphics graphics, Font font, int tickCount, int mouseX, int mouseY,
                            boolean focused, boolean hovered, CallbackInfo ci) {
        graphics.pose().pushMatrix();
        schat$translate(graphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void schat$pop(GuiGraphics graphics, Font font, int tickCount, int mouseX, int mouseY,
                           boolean focused, boolean hovered, CallbackInfo ci) {
        graphics.pose().popMatrix();
        schat$after(graphics, mouseX, mouseY, tickCount, focused, hovered);
    }
    //?}

    // Запись в Options дёргает onValueUpdate -> rescaleChat, а тот сбрасывает прокрутку.
    //? if >=1.21.11 && <26.1 {
    @Redirect(method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = TEXT_BACKGROUND_OPACITY_READ))
    private Object schat$backgroundOpacity(OptionInstance<?> instance) {
        ChatPanel panel = ChatTabs.panelOf(this);
        Double custom = panel == null || panel.empty()
                ? null
                : panel.activeTab().backgroundOpacity();
        return custom != null ? custom : instance.get();
    }
    //?}

    //? if >=1.21.6 && <1.21.11 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void schat$push(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                            boolean focused, CallbackInfo ci) {
        graphics.pose().pushMatrix();
        schat$translate(graphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void schat$pop(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                           boolean focused, CallbackInfo ci) {
        graphics.pose().popMatrix();
        schat$after(graphics, mouseX, mouseY, tickCount, focused, false);
    }
    *///?}

    //? if >=1.20.5 && <1.21.6 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void schat$push(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                            boolean focused, CallbackInfo ci) {
        graphics.pose().pushPose();
        schat$translate(graphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void schat$pop(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                           boolean focused, CallbackInfo ci) {
        graphics.pose().popPose();
        schat$after(graphics, mouseX, mouseY, tickCount, focused, false);
    }
    *///?}

    //? if <1.20.5 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void schat$push(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                            CallbackInfo ci) {
        graphics.pose().pushPose();
        schat$translate(graphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void schat$pop(GuiGraphics graphics, int tickCount, int mouseX, int mouseY,
                           CallbackInfo ci) {
        graphics.pose().popPose();
        schat$after(graphics, mouseX, mouseY, tickCount, isChatFocused(), false);
    }
    *///?}

    //? if >=26.1 {
    /*@Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            at = @At("HEAD"))
    private void schat$push(GuiGraphicsExtractor graphics, Font font, int tickCount, int mouseX,
                            int mouseY, ChatComponent.DisplayMode mode, boolean hovered,
                            CallbackInfo ci) {
        graphics.pose().pushMatrix();
        ChatPanel panel = ChatTabs.panelOf(this);
        if (panel != null && !panel.empty()) {
            graphics.pose().translate(panel.effectiveOffsetX(), panel.effectiveOffsetY());
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",
            at = @At("RETURN"))
    private void schat$pop(GuiGraphicsExtractor graphics, Font font, int tickCount, int mouseX,
                           int mouseY, ChatComponent.DisplayMode mode, boolean hovered,
                           CallbackInfo ci) {
        graphics.pose().popMatrix();
        ChatPanel panel = ChatTabs.panelOf(this);
        if (panel == null || !panel.primary()) {
            return;
        }
        for (ChatPanel other : SchatConfig.get().panels()) {
            if (other == panel || other.empty()) {
                continue;
            }
            ChatComponent component = other.component();
            if (component != null) {
                component.extractRenderState(graphics, font, tickCount, mouseX, mouseY, mode,
                        hovered);
            }
        }
        TabStrip.renderAll(graphics::fill, graphics::text, isChatFocused(), mouseX, mouseY);
    }

    @Redirect(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
                    ordinal = 1))
    private Object schat$backgroundOpacity(OptionInstance<?> instance) {
        ChatPanel panel = ChatTabs.panelOf(this);
        Double custom = panel == null || panel.empty()
                ? null
                : panel.activeTab().backgroundOpacity();
        return custom != null ? custom : instance.get();
    }
    *///?}

    //? if <26.1 {
    @Unique
    private void schat$translate(GuiGraphics graphics) {
        ChatPanel panel = ChatTabs.panelOf(this);
        if (panel == null || panel.empty()) {
            return;
        }
        int offsetX = panel.effectiveOffsetX();
        int offsetY = panel.effectiveOffsetY()
                + ChatFrame.stateOffsetY(panel, isChatFocused());
        /*? if >=1.21.6 {*/
        graphics.pose().translate(offsetX, offsetY);
        /*?} else {*/
        /*graphics.pose().translate(offsetX, offsetY, 0.0f);
        *//*?}*/
    }

    // Ваниль рисует только свой экземпляр, остальные панели дорисовываем отсюда.
    @Unique
    private void schat$after(GuiGraphics graphics, int mouseX, int mouseY, int tickCount,
                             boolean focused, boolean hovered) {
        ChatPanel panel = ChatTabs.panelOf(this);
        if (panel == null || !panel.primary()) {
            return;
        }
        for (ChatPanel other : SchatConfig.get().panels()) {
            if (other == panel || other.empty()) {
                continue;
            }
            ChatComponent component = other.component();
            if (component != null) {
                schat$renderPanel(component, graphics, tickCount, mouseX, mouseY, focused, hovered);
            }
        }
        TabStrip.renderAll(graphics::fill, graphics::drawString, isChatFocused(), mouseX, mouseY);
    }

    @Unique
    private void schat$renderPanel(ChatComponent component, GuiGraphics graphics, int tickCount,
                                   int mouseX, int mouseY, boolean focused, boolean hovered) {
        //? if >=1.21.11 {
        component.render(graphics, net.minecraft.client.Minecraft.getInstance().font, tickCount,
                mouseX, mouseY, focused, hovered);
        //?} else if >=1.20.5 {
        /*component.render(graphics, tickCount, mouseX, mouseY, focused);
        *///?} else {
        /*component.render(graphics, tickCount, mouseX, mouseY);
        *///?}
    }
    //?}
}
