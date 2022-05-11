package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.commons.message.*;

import java.io.IOException;
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

        // Пришло сообщение об авторизации
        if (msg instanceof SignInMessage) {
            SignInMessageHandler(ctx, (SignInMessage) msg, signAnswer, textAnswer);
        }

        // Пришло сообщение о регистрации нового пользователя
        if (msg instanceof SignUpMessage) {
            singUpMessageHandler(ctx, (SignUpMessage) msg, signAnswer, textAnswer);
        }

        // Пришло сообщение о смене текущей папки клиента на сервере
        if (msg instanceof RequestUpdateFileListMessage) {
            RequestUpdateFileListMessage requestUpdateFileListMessage = (RequestUpdateFileListMessage) msg;
            boolean b = requestUpdateFileListMessage.getPath().equals("");
            if (!b) {
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
        }

        // Пришло сообщение о запросе на создание новой папки на сервере
        if (msg instanceof RequestCreateDirectoryMessage) {
            RequestCreateDirectoryMessage rcdm = (RequestCreateDirectoryMessage) msg;

            try {
                Files.createDirectory(Paths.get(dirCurrentClient + "\\"+ rcdm.getNewDir()));
                ctx.writeAndFlush(updateListFile(dirCurrentClient));
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            fldDirCurrentClient.setFldDir("..");
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
            fldDirCurrentClient.setFldDir("..");
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