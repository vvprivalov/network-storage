import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.gb.storage.client.FirstClientHandler;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.SignInMessage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartWindowController implements Initializable {
    SignInMessage signInMessage;
    ChannelFuture channelFuture;

    @FXML
    private Button btnSignIn;

    @FXML
    private Button btnSignUp;

    @FXML
    private TextField fldSignInLogin;

    @FXML
    private PasswordField fldSignInPassword;

    @FXML
    private TextField fldSignUpFirstName;

    @FXML
    private TextField fldSignUpLastName;

    @FXML
    private TextField fldSignUpLogin;

    @FXML
    private PasswordField fldSignUpPassword;

    @FXML
    private Label lblMessage;

    @FXML
    void SignUpFunc(ActionEvent event) {

    }

    @FXML
    void signInFunc(ActionEvent event) {
        signInMessage = new SignInMessage();
        signInMessage.setLogin(fldSignInLogin.getText());
        signInMessage.setPassword(fldSignInPassword.getText());
        channelFuture.channel().writeAndFlush(signInMessage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ExecutorService threadpool = Executors.newFixedThreadPool(1);

        threadpool.execute(() -> Connect());
    }

    public void Connect() {
        final NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0,
                                            3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new FirstClientHandler());
                        }
                    });

            System.out.println("Клиент запущен");

            channelFuture = bootstrap.connect("localhost", 9000).sync();

            while (channelFuture.channel().isActive()) {

            }

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

