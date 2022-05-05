package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Label;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.TextMessage;

public class FirstClientHandler extends SimpleChannelInboundHandler<Message> {
    private final Label lblMessage;
    public FirstClientHandler(Label lblMessage) {

        this.lblMessage = lblMessage;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {
        if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            Platform.runLater(() -> lblMessage.setText(msg.getText()));
        }
    }
}
