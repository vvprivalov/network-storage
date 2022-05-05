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
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // Пришло сообщение об авторизации
        if (msg instanceof SignInMessage) {
            SignInMessage message = (SignInMessage) msg;
            boolean isDone = useDBforAuth.checkLoginAndPasswordAtIdentification(message.getLogin(), message.getPassword());
            TextMessage answer = new TextMessage();
            if (isDone) {
                answer.setText("Вы аутентифицированы. Ожидайте вход с систему.....");
                ctx.writeAndFlush(answer);
            } else {
                answer.setText("Пользователь [ " + message.getLogin() + " ] или пароль не верны!!!");
                ctx.writeAndFlush(answer);
            }
        }

        // Пришло сообщение о регистрации нового пользователя
        if (msg instanceof SignUpMessage) {
            SignUpMessage message = (SignUpMessage) msg;
            TextMessage answer = new TextMessage();
            boolean isDone = useDBforAuth.newUserRegistration(message.getLogin(), message.getPassword(),
                    message.getFirstName(), message.getLastName());
            if (isDone) {
                answer.setText("Вы зарегистрированы. Ожидайте вход с систему.....");
            } else {
                answer.setText("Пользователь [ " + message.getLogin() + " ] уже зарегистрирован");
            }
            ctx.writeAndFlush(answer);
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