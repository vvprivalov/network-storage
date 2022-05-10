package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import ru.gb.storage.commons.message.FileListMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.SignAnswer;
import ru.gb.storage.commons.message.TextInfoMessage;
import java.io.IOException;

public class FirstClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {
        // Получение от сервера текстового сообщения для вывода пользователю внизу окна
        if (message instanceof TextInfoMessage) {
            TextInfoMessage msg = (TextInfoMessage) message;
            Platform.runLater(() -> Client.startController.lblMessage.setText(msg.getText()));
        }
        // Получение от сервера сообщения об успешности авторизации и регистрации
        if (message instanceof SignAnswer) {
            SignAnswer answer = (SignAnswer) message;
            if (answer.isbAnswer()) {
                Platform.runLater(() -> {
                    try {
                        Client.startController.startMainWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        // Получение от сервера сообщения о получении списка файлов в каталоге клиента
        if (message instanceof FileListMessage) {
            FileListMessage fileListMessage = (FileListMessage) message;
            Platform.runLater(() -> {
                Client.mainController.updateListRight(fileListMessage);
            });
            System.out.println(fileListMessage);
        }
    }
}
