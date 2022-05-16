package ru.gb.storage.client;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.gb.storage.commons.message.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;

public class FirstClientHandler extends SimpleChannelInboundHandler<Message> {
    private RandomAccessFile accessFile;

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

        // Пришло сообщение о передаче файла с сервера на сторону клиента
        if (message instanceof TransferFileMessage) {
            TransferFileMessage msg = (TransferFileMessage) message;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                    Client.mainController.leftFldPath.getText() + "\\" + msg.getFileName(), "rw")) {
                randomAccessFile.seek(msg.getStartPosition());
                randomAccessFile.write(msg.getContent());
                if (msg.isLast()) {
                    Platform.runLater(() -> Client.mainController.outputMessage("Файл скопирован на ваш ПК"));
                    Client.mainController.updateListLeft(Paths.get(Client.mainController.leftFldPath.getText()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Сообщение от сервера о невозможно удаления файла или папки
        if (message instanceof AnswerActionFileMessage) {
            AnswerActionFileMessage answer = (AnswerActionFileMessage) message;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, answer.getAnswer(),
                        ButtonType.OK);
                alert.setHeaderText("Сообщение");
                alert.showAndWait();
            });
        }

        // Пришло сообщение о наличии файла для копирования
        if (message instanceof AnswerExistFileMessage) {
            AnswerExistFileMessage answerExistFileMessage = (AnswerExistFileMessage) message;
            String fileName = answerExistFileMessage.getFileName();
            File file = answerExistFileMessage.getFile();

            if (answerExistFileMessage.isExist()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Вы действительно хотите заменить файл на сервере?", ButtonType.OK, ButtonType.CANCEL);
                    alert.setTitle("Замена файла");
                    alert.setHeaderText("Будьте внимательны!");
                    alert.showAndWait();
                    if (alert.getResult().getText().equals("OK")) {
                        try {
                            accessFile = new RandomAccessFile(file, "r");
                            sendFile(fileName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                try {
                    accessFile = new RandomAccessFile(file, "r");
                    sendFile(fileName);
                } catch (FileNotFoundException e) {
                    Platform.runLater(() -> Client.mainController.outputMessage("Не удалось открыть файл, возможно это папка!"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void sendFile(String fileName) throws IOException {
        if (accessFile != null) {
            byte[] fileContent;
            long availableBytes = accessFile.length() - accessFile.getFilePointer();
            int BUFFER_SIZE = 64 * 1024;
            if (availableBytes > BUFFER_SIZE) {
                fileContent = new byte[BUFFER_SIZE];
            } else {
                fileContent = new byte[(int) availableBytes];
            }
            TransferFileMessage message = new TransferFileMessage();
            message.setStartPosition(accessFile.getFilePointer());
            accessFile.read(fileContent);
            message.setContent(fileContent);
            message.setFileName(fileName);
            final boolean last = accessFile.getFilePointer() == accessFile.length();
            message.setLast(last);
            if (last) {
                accessFile.close();
                accessFile = null;
            }
            Client.startController.channelFuture.channel().writeAndFlush(message)
                    .addListener((ChannelFutureListener) channelFuture -> {
                        if (!last) {
                            sendFile(fileName);
                        }
                    });
        }
    }
}
