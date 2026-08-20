package hw.smoup.schat.client.chat;

import hw.smoup.schat.client.config.ChatPanel;

public interface PanelBound {

    ChatPanel schat$panel();

    void schat$setPanel(ChatPanel panel);
}
