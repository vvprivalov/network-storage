package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private final UseDBforAuth useDBforAuth;

    public FirstServerHandler(UseDBforAuth useDBforAuth) {
        this.useDBforAuth = useDBforAuth;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Новый канал активирован");
        TextMessage answer = new TextMessage();
        answer.setText("Успешное соединение с сервером");
        ctx.writeAndFlush(answer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // Пришло сообщение об авторизации
        if (msg instanceof SignInMessage) {
            SignInMessage message = (SignInMessage) msg;
            String login = message.getLogin();
            String password = message.getPassword();
            boolean isDone = useDBforAuth.checkLoginAndPasswordAtIdentification(login, password);
            if (isDone) {
                System.out.println("Вы вошли в сетевое хранилище");
            } else {
                System.out.println("Пользователь с таким логином не зарегистрирован");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        System.out.println("Клиент отключился");
    }
}