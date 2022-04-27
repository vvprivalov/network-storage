import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class StartWindowController implements Initializable {

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

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
