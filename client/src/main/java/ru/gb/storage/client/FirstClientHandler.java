package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.gb.storage.commons.message.*;

import java.io.IOException;

public class FirstClientHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {
        // Текстовое сообщение от сервера для вывода пользователю внизу окна
        if (message instanceof TextInfoMessage) {
            TextInfoMessage msg = (TextInfoMessage) message;
            Platform.runLater(() -> Client.startController.lblMessage.setText(msg.getText()));
        }
        // Сообщение от сервера об успешности авторизации и регистрации
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

        // Сообщение от сервера, в котором пришел список файлов в каталоге клиента
        if (message instanceof FileListMessage) {
            FileListMessage fileListMessage = (FileListMessage) message;
            Platform.runLater(() -> Client.mainController.updateListRight(fileListMessage));
        }

        // Сообщение от сервера о текущей папки клиента, которая прописывается в верхнем текстовом поле.
        if (message instanceof FldDirClientMessage) {
            FldDirClientMessage fldDirClientMessage = (FldDirClientMessage) message;
            Platform.runLater(() -> Client.mainController.rightFldPath.setText(fldDirClientMessage.getFldDir()));
        }

        // Сообщение от сервера о невозможно удаления файла или папки
        if (message instanceof AnswerActionFileMessage) {
            AnswerActionFileMessage answer = (AnswerActionFileMessage) message;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, answer.getAnswer(),
                        ButtonType.OK);
                alert.setHeaderText("Предупреждение");
                alert.showAndWait();});
        }
    }
}
