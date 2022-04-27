package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.SignInMessage;
import ru.gb.storage.commons.message.TextMessage;

public class FirstClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            System.out.println("Получено сообщение от сервера: " + msg.getText());
        }
    }
}
