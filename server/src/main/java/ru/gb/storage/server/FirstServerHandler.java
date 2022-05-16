package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {
    private final UseDBforAuth useDBforAuth;
    private Path dirRootClient;
    private Path dirCurrentClient;
    private String fldPath;
    private FldDirClientMessage fldDirCurrentClient;

    public FirstServerHandler(UseDBforAuth useDBforAuth) {

        this.useDBforAuth = useDBforAuth;
        fldDirCurrentClient = new FldDirClientMessage();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        System.out.println("Новый канал активирован");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        SignAnswer signAnswer = new SignAnswer();
        TextInfoMessage textAnswer = new TextInfoMessage();

        // Пришел запрос на сервер о проверке наличия указанного файла
        if (msg instanceof RequestExistFileMessage) {
            RequestExistFileMessage requestExistFileMessage = (RequestExistFileMessage) msg;
            AnswerExistFileMessage answerExistFileMessage = new AnswerExistFileMessage();
            if (Files.exists(Paths.get(dirCurrentClient + "\\" + requestExistFileMessage.getFileName()))) {
                answerExistFileMessage.setExist(true);
                answerExistFileMessage.setFileName(requestExistFileMessage.getFileName());
                answerExistFileMessage.setFile(requestExistFileMessage.getFile());
                System.out.println("Такой файл существует");
            } else {
                answerExistFileMessage.setExist(false);
                answerExistFileMessage.setFileName(requestExistFileMessage.getFileName());
                answerExistFileMessage.setFile(requestExistFileMessage.getFile());
                System.out.println("Такой файл отсутствует");
            }
            try {
                ctx.writeAndFlush(answerExistFileMessage).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Пришло сообщение о передаче файла на сторону сервера
        if (msg instanceof TransferFileMessage) {
            TransferFileMessage message = (TransferFileMessage) msg;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(dirCurrentClient + "\\" +
                    message.getFileName(), "rw")) {
                randomAccessFile.seek(message.getStartPosition());
                randomAccessFile.write(message.getContent());
                if (message.isLast()) {
                    AnswerActionFileMessage answer = new AnswerActionFileMessage();
                    answer.setAnswer("Файл скопирован на сервер");
                    ctx.writeAndFlush(answer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ctx.writeAndFlush(updateListFile(dirCurrentClient));
        }

        // Пришло сообщение об авторизации
        if (msg instanceof SignInMessage) {
            SignInMessageHandler(ctx, (SignInMessage) msg, signAnswer, textAnswer);
        }

        // Пришло сообщение о регистрации нового пользователя
        if (msg instanceof SignUpMessage) {
            singUpMessageHandler(ctx, (SignUpMessage) msg, signAnswer, textAnswer);
        }

        // Пришло сообщение о смене текущей папки клиента на сервере
        // Если приходит пустая строка, то идем по дереву вверх, есть путь, то идем по дереву вниз.
        if (msg instanceof RequestUpdateFileListMessage) {
            int lenght = dirRootClient.toString().length();
            RequestUpdateFileListMessage requestUpdateFileListMessage = (RequestUpdateFileListMessage) msg;
            if (!requestUpdateFileListMessage.getPath().equals("")) {
                Path path = Paths.get(dirCurrentClient.toString() + "\\" + requestUpdateFileListMessage.getPath());
                if (Files.isDirectory(path)) {
                    dirCurrentClient = path;
                    ctx.writeAndFlush(updateListFile(dirCurrentClient));
                }
            } else {
                if (!dirCurrentClient.equals(dirRootClient)) {
                    dirCurrentClient = dirCurrentClient.getParent();
                    ctx.writeAndFlush(updateListFile(dirCurrentClient));
                }

            }
            if (!dirCurrentClient.equals(dirRootClient)) {
                fldDirCurrentClient.setFldDir("Корневая папка клиента:\\ " + dirCurrentClient.toString().substring(lenght + 1));
                ctx.writeAndFlush(fldDirCurrentClient);
            } else {
                fldDirCurrentClient.setFldDir("Корневая папка клиента:\\ ");
                ctx.writeAndFlush(fldDirCurrentClient);
            }
        }

        // Пришло сообщение с запросом на создание новой папки на сервере
        if (msg instanceof RequestCreateDirectoryMessage) {
            RequestCreateDirectoryMessage rcdm = (RequestCreateDirectoryMessage) msg;

            try {
                Files.createDirectory(Paths.get(dirCurrentClient + "\\" + rcdm.getNewDir()));
                ctx.writeAndFlush(updateListFile(dirCurrentClient));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Пришло сообщение с запросом на удаление файла или папки на сервере
        if (msg instanceof RequestDeleteFileMessage) {
            RequestDeleteFileMessage rdfm = (RequestDeleteFileMessage) msg;
            try {
                Files.delete(Paths.get(dirCurrentClient + "\\" + rdfm.getFileName()));
            } catch (IOException e) {
                AnswerActionFileMessage message = new AnswerActionFileMessage();
                message.setAnswer("Файл не удален, возможно это папка и она не пустая!!!");
                ctx.writeAndFlush(message);
            }
            ctx.writeAndFlush(updateListFile(dirCurrentClient));
        }

    }

    // Метод, который обрабатывает входящее сообщение с запросом на авторизацию существующего пользователя
    private void SignInMessageHandler(ChannelHandlerContext ctx, SignInMessage msg, SignAnswer signAnswer, TextInfoMessage textAnswer) {
        SignInMessage message = (SignInMessage) msg;
        boolean isDone = useDBforAuth.checkLoginAndPasswordAtIdentification(message.getLogin(),
                message.getPassword());
        if (isDone) {
            textAnswer.setText("Вы аутентифицированы. Ожидайте вход с систему.....");
            ctx.writeAndFlush(textAnswer);
            signAnswer.setbAnswer(true);
            ctx.writeAndFlush(signAnswer);
            dirRootClient = Path.of("C:\\Storage\\" + "Dir_" + message.getLogin().trim() + "_client");
            dirCurrentClient = dirRootClient;
            ctx.writeAndFlush(updateListFile(dirRootClient));
            fldDirCurrentClient.setFldDir("Корневая папка клиента");
            ctx.writeAndFlush(fldDirCurrentClient);
        } else {
            textAnswer.setText("Пользователь [ " + message.getLogin() + " ] или пароль не верны!!!");
            ctx.writeAndFlush(textAnswer);
            signAnswer.setbAnswer(false);
            ctx.writeAndFlush(signAnswer);
        }
    }

    // Метод, который обрабатывает входящее сообщение с запросом на регистрацию нового клиента
    private void singUpMessageHandler(ChannelHandlerContext ctx, SignUpMessage msg, SignAnswer signAnswer,
                                      TextInfoMessage textAnswer) {
        SignUpMessage message = (SignUpMessage) msg;
        boolean isDone = useDBforAuth.newUserRegistration(message.getLogin(), message.getPassword(),
                message.getFirstName(), message.getLastName());
        if (isDone) {
            textAnswer.setText("Вы зарегистрированы. Ожидайте вход с систему.....");
            ctx.writeAndFlush(textAnswer);
            signAnswer.setbAnswer(true);
            ctx.writeAndFlush(signAnswer);
            dirRootClient = Path.of("C:\\Storage\\" + "Dir_" + message.getLogin().trim() + "_client");
            dirCurrentClient = dirRootClient;
            fldDirCurrentClient.setFldDir("Корневая папка клиента");
            ctx.writeAndFlush(fldDirCurrentClient);
            try {
                Files.createDirectory(dirRootClient);
            } catch (IOException e) {
                System.out.println("Не удалось создать папку для нового пользователя");
            }
            ctx.writeAndFlush(updateListFile(dirRootClient));
        } else {
            textAnswer.setText("Пользователь [ " + message.getLogin() + " ] уже зарегистрирован");
            ctx.writeAndFlush(textAnswer);
            signAnswer.setbAnswer(false);
            ctx.writeAndFlush(signAnswer);
        }
    }

    // Метод, который формирует список файлов в папке клиента на сервере и отправляет FileListMessage клиенту
    public FileListMessage updateListFile(Path dirRootClient) {
        FileListMessage fileList = new FileListMessage();
        List<FileInfoMessage> fileInfoMessageList = new ArrayList<>();
        try {
            ArrayList<Path> listPath = (ArrayList<Path>) Files.list(dirRootClient).collect(Collectors.toList());
            for (Path pth : listPath) {
                FileInfoMessage fileInfoMessage = new FileInfoMessage();
                fileInfoMessage.fillInfoFile(pth);
                fileInfoMessageList.add(fileInfoMessage);
            }
            fileList.setListFile(fileInfoMessageList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
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