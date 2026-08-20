package hw.smoup.schat.mixin.client;

import hw.smoup.schat.client.chat.PanelBound;
import hw.smoup.schat.client.config.ChatPanel;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

// Ваниль режет историю сотней в двух местах: по сообщениям и по строкам после
// переноса. Второй лимит и есть реальная глубина скролла, поэтому его берём
// с запасом — длинное сообщение занимает несколько строк.
@Mixin(ChatComponent.class)
public abstract class ChatComponentHistoryMixin {

    private static final int LINES_PER_MESSAGE = 4;

    // До 1.20.5 обрезка жила прямо в addMessage, отдельных очередей не было: там
    // история остаётся ванильной сотней.
    //? if >=1.20.5 {
    @ModifyConstant(method = "addMessageToQueue", constant = @Constant(intValue = 100))
    private int schat$messageLimit(int original) {
        int limit = historyLimit();
        return limit > 0 ? limit : original;
    }

    @ModifyConstant(method = "addMessageToDisplayQueue", constant = @Constant(intValue = 100))
    private int schat$lineLimit(int original) {
        int limit = historyLimit();
        return limit > 0 ? limit * LINES_PER_MESSAGE : original;
    }

    @Unique
    private int historyLimit() {
        ChatPanel panel = ((PanelBound) this).schat$panel();
        return panel == null || panel.empty() ? 0 : panel.activeTab().historyLimit();
    }
    //?}
}
