package ru.gb.storage.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import ru.gb.storage.commons.message.FileInfoMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private ComboBox<String> leftComboDisk;

    @FXML
    private TextField leftFldPath;

    @FXML
    private TableView<FileInfoMessage> leftTableView;

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
    private TextField rightFldPath;

    @FXML
    private TableView<FileInfoMessage> rightTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Формирование левой панели
        // Первый столбец "Имя"
        TableColumn<FileInfoMessage, String> leftFileNameColumn = new TableColumn<>("Имя");
        leftFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        leftFileNameColumn.setPrefWidth(140);

        // Второй столбец "Размер"
        TableColumn<FileInfoMessage, Long> leftFileSizeColumn = new TableColumn<>("Размер");
        leftFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getFileSize()));
        leftFileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfoMessage, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if (aLong == null || b) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if (aLong == -1L) {
                            text = "[ DIR ]";
                        }
                        setText(text);
                    }
                }
            };
        });
        leftFileSizeColumn.setPrefWidth(70);

        // Третий столбец "Дата"
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfoMessage, String> leftFileDateColumn = new TableColumn<>("Дата изменения");
        leftFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        leftFileDateColumn.setPrefWidth(120);


        // Формирование правой панели
        TableColumn<FileInfoMessage, String> rightFileNameColumn = new TableColumn<>("Имя");
        rightFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        rightFileNameColumn.setPrefWidth(140);

        TableColumn<FileInfoMessage, Long> rightFileSizeColumn = new TableColumn<>("Размер");
        rightFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getFileSize()));
        rightFileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfoMessage, Long>() {
                @Override
                protected void updateItem(Long aLong, boolean b) {
                    super.updateItem(aLong, b);
                    if (aLong == null || b) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", aLong);
                        if (aLong == -1L) {
                            text = "[ DIR ]";
                        }
                        setText(text);
                    }
                }
            };
        });
        rightFileSizeColumn.setPrefWidth(70);

        // Третий столбец "Дата"
        DateTimeFormatter dtfR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfoMessage, String> rightFileDateColumn = new TableColumn<>("Дата изменения");
        rightFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        rightFileDateColumn.setPrefWidth(120);


        leftTableView.getColumns().addAll(leftFileNameColumn, leftFileSizeColumn, leftFileDateColumn);
        leftTableView.getSortOrder().add(leftFileSizeColumn);

        rightTableView.getColumns().addAll(rightFileNameColumn, rightFileSizeColumn, rightFileDateColumn);
        leftTableView.getSortOrder().add(rightFileSizeColumn);

        // Формирование комбобокса для отображения списка дисков
        leftComboDisk.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            leftComboDisk.getItems().add(p.toString());
        }
        leftComboDisk.getSelectionModel().select(0);

        // Метод, который по двойному клику мыши заходит в каталог
        leftTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(leftFldPath.getText()).resolve(leftTableView.getSelectionModel()
                            .getSelectedItem().getFileName());
                    if (Files.isDirectory(path)) {
                        updateList(path, 1);
                    }
                }
            }
        });

        // Обновление списка файлов левой панели
        updateList(Paths.get("."), 1);
        // Обновление списка файлов правой панели
        updateList(Paths.get("D:\\"), 2);
    }

    // метод, который обновляет список файлов в правой и левой таблице
    public void updateList(Path path, int side) {
        try {
            if (side == 1) {
                leftFldPath.setText(path.normalize().toAbsolutePath().toString());
                leftTableView.getItems().clear();
                leftTableView.getItems().addAll(Files.list(path).map(FileInfoMessage::new).collect(Collectors.toList()));
                leftTableView.sort();
            } else if (side == 2) {
                rightFldPath.setText(path.normalize().toAbsolutePath().toString());
                rightTableView.getItems().clear();
                rightTableView.getItems().addAll(Files.list(path).map(FileInfoMessage::new).collect(Collectors.toList()));
                rightTableView.sort();

            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(leftFldPath.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath, 1);
        }
    }

    public void leftComboAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()), 1);
    }
}
