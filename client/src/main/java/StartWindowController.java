import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import ru.gb.storage.client.Client;
import ru.gb.storage.client.FirstClientHandler;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.SignInMessage;
import ru.gb.storage.commons.message.SignUpMessage;

import java.io.IOException;
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
    void SignUpFunc(ActionEvent event) throws IOException {
        if (fldSignUpLastName.getText().equals("") | fldSignUpFirstName.getText().equals("") |
                fldSignUpLogin.getText().equals("") | fldSignUpPassword.getText().equals("")) {
            lblMessage.setText("Все поля формы должны быть заполнены");
            return;
        }
        SignUpMessage signUpMessage = new SignUpMessage();
        signUpMessage.setLogin(fldSignUpLogin.getText());
        signUpMessage.setPassword(fldSignUpPassword.getText());
        signUpMessage.setFirstName(fldSignUpFirstName.getText());
        signUpMessage.setLastName(fldSignUpLastName.getText());
        channelFuture.channel().writeAndFlush(signUpMessage);

        // Заминка, ну сразу обновляется строка, поэтому, чтобы зайти в основное окно, приходится дважды кликать
        // КАК ЭТО УСТРАНИТЬ?????
        if (lblMessage.getText().startsWith("Вы")) {
            startMainWindow(event, fldSignUpLogin);
        }
    }

    @FXML
    void signInFunc(ActionEvent event) throws IOException {
        signInMessage = new SignInMessage();
        signInMessage.setLogin(fldSignInLogin.getText());
        signInMessage.setPassword(fldSignInPassword.getText());
        channelFuture.channel().writeAndFlush(signInMessage);

        // Заминка, ну сразу обновляется строка, поэтому, чтобы зайти в основное окно, приходится дважды кликать
        // КАК ЭТО УСТРАНИТЬ?????
        if (lblMessage.getText().startsWith("Вы")) {
            startMainWindow(event, fldSignInLogin);
        }
    }

    // метод запускающий основное окно хранилища
    private void startMainWindow(ActionEvent event, TextField login) throws IOException {
        Stage primaryStage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/Main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        primaryStage.setTitle("Сетевое хранилище");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ExecutorService threadpool = Executors.newFixedThreadPool(1);

        threadpool.execute(this::Connect);
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
                                    new FirstClientHandler(lblMessage));
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

