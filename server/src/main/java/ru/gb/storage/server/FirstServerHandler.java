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
        SignAnswer signAnswer = new SignAnswer();
        TextMessage textAnswer = new TextMessage();

        // Пришло сообщение об авторизации
        if (msg instanceof SignInMessage) {
            SignInMessage message = (SignInMessage) msg;
            boolean isDone = useDBforAuth.checkLoginAndPasswordAtIdentification(message.getLogin(),
                    message.getPassword());
            if (isDone) {
                signAnswer.setbAnswer(true);
                textAnswer.setText("Вы аутентифицированы. Ожидайте вход с систему.....");
            } else {
                signAnswer.setbAnswer(false);
                textAnswer.setText("Пользователь [ " + message.getLogin() + " ] или пароль не верны!!!");
            }
            ctx.writeAndFlush(textAnswer);
            ctx.writeAndFlush(signAnswer);
        }

        // Пришло сообщение о регистрации нового пользователя
        if (msg instanceof SignUpMessage) {
            SignUpMessage message = (SignUpMessage) msg;
            boolean isDone = useDBforAuth.newUserRegistration(message.getLogin(), message.getPassword(),
                    message.getFirstName(), message.getLastName());
            if (isDone) {
                signAnswer.setbAnswer(true);
                textAnswer.setText("Вы зарегистрированы. Ожидайте вход с систему.....");
            } else {
                signAnswer.setbAnswer(false);
                textAnswer.setText("Пользователь [ " + message.getLogin() + " ] уже зарегистрирован");
            }
            ctx.writeAndFlush(textAnswer);
            ctx.writeAndFlush(signAnswer);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) { System.out.println("Клиент отключился"); }
}