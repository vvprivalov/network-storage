package ru.gb.storage.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Button leftBtnChange;

    @FXML
    private Button leftBtnCopy;

    @FXML
    private Button leftBtnDelete;

    @FXML
    private Button leftBtnMove;

    @FXML
    private Button leftBtnUp;

    @FXML
    private ComboBox<?> leftComboDisk;

    @FXML
    private TextField leftFldPath;

    @FXML
    private TableView<?> leftTableView;

    @FXML
    private Button rightBtnChange;

    @FXML
    private Button rightBtnCopy;

    @FXML
    private Button rightBtnDelete;

    @FXML
    private Button rightBtnMove;

    @FXML
    private Button rightBtnUp;

    @FXML
    private ComboBox<?> rightComboDisk;

    @FXML
    private TextField rightFldPath;

    @FXML
    private TableView<?> rightTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
