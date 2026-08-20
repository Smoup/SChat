package hw.smoup.schat.mixin.client;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
//? if >=26.1 {
/*import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.gen.Invoker;
*///?}

@Mixin(ChatComponent.class)
public interface ChatComponentAccessor {

    @Accessor("allMessages")
    List<GuiMessage> schat$allMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> schat$trimmedMessages();

    //? if >=26.1 {
    /*@Invoker("addMessage")
    void schat$addMessage(Component message, MessageSignature signature, GuiMessageSource source,
                          GuiMessageTag tag);
    *///?}
}
