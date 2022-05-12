package ru.gb.storage.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ru.gb.storage.commons.message.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private Button rightBtnCreate;

    @FXML
    private Button rightBtnCopy;

    @FXML
    private Button rightBtnDelete;

    @FXML
    private Button rightBtnMove;

    @FXML
    private Button rightBtnUp;

    @FXML
    public TextField rightFldPath;

    @FXML
    private TableView<FileInfoMessage> rightTableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Формирование левой панели
        TableColumn<FileInfoMessage, String> leftFileNameColumn = new TableColumn<>("Имя");
        leftFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        leftFileNameColumn.setPrefWidth(240);

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
        leftFileSizeColumn.setPrefWidth(100);

        TableColumn<FileInfoMessage, String> leftFileDateColumn = new TableColumn<>("Дата изменения");
        leftFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        leftFileDateColumn.setPrefWidth(120);

        // Формирование правой панели
        TableColumn<FileInfoMessage, String> rightFileNameColumn = new TableColumn<>("Имя");
        rightFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        rightFileNameColumn.setPrefWidth(240);

        TableColumn<FileInfoMessage, Long> rightFileSizeColumn = new TableColumn<>("Размер");
        rightFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getFileSize()));
        rightFileSizeColumn.setCellFactory(column -> new TableCell<FileInfoMessage, Long>() {
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
        });
        rightFileSizeColumn.setPrefWidth(100);

        TableColumn<FileInfoMessage, String> rightFileDateColumn = new TableColumn<>("Дата изменения");
        rightFileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified()));
        rightFileDateColumn.setPrefWidth(120);

        // Добавляем сформированные колонки в таблицу
        leftTableView.getColumns().addAll(leftFileNameColumn, leftFileSizeColumn, leftFileDateColumn);
        leftTableView.getSortOrder().add(leftFileSizeColumn);
        rightTableView.getColumns().addAll(rightFileNameColumn, rightFileSizeColumn, rightFileDateColumn);
        rightTableView.getSortOrder().add(rightFileSizeColumn);

        // Формирование левого комбобокса для отображения списка дисков
        leftComboDisk.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            leftComboDisk.getItems().add(p.toString());
        }
        leftComboDisk.getSelectionModel().select(0);

        // Метод, который по двойному клику мыши заходит в каталог для левой панели
        leftTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && leftTableView.getSelectionModel().getSelectedItem() != null) {
                Path path = Paths.get(leftFldPath.getText()).resolve(leftTableView.getSelectionModel()
                        .getSelectedItem().getFileName());
                boolean b = Files.isDirectory(path);
                if (Files.isDirectory(path)) {
                    updateListLeft(path);
                }
            }
        });

        // Метод, который по двойному клику мыши заходит в каталог для правой панели
        rightTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && rightTableView.getSelectionModel().getSelectedItem() != null) {
                Path path = Paths.get(rightTableView.getSelectionModel().getSelectedItem().getFileName());
                RequestUpdateFileListMessage requestUpdateFileListMessage = new RequestUpdateFileListMessage();
                requestUpdateFileListMessage.setPath(path.toString());
                Client.startController.channelFuture.channel().writeAndFlush(requestUpdateFileListMessage);
            }
        });

        // Обновление списка файлов левой панели
        updateListLeft(Paths.get("."));
    }

    // метод, который обновляет список файлов в левой таблице
    public void updateListLeft(Path path) {
        leftFldPath.setText(path.normalize().toAbsolutePath().toString());
        leftTableView.getItems().clear();
        try {
            ArrayList<Path> listPath = (ArrayList<Path>) Files.list(path).collect(Collectors.toList());
            for (Path pth : listPath) {
                FileInfoMessage fileInfoMessage = new FileInfoMessage();
                fileInfoMessage.fillInfoFile(pth);
                leftTableView.getItems().add(fileInfoMessage);
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов",
                    ButtonType.OK);
            alert.setHeaderText("Сообщение");
            alert.showAndWait();
        }
        leftTableView.sort();
    }

    // Метод, который обновляет список файлов в правой таблице
    public void updateListRight(FileListMessage fileList) {
        rightTableView.getItems().clear();
        rightTableView.getItems().addAll(fileList.getListFile());
        rightTableView.sort();
    }

    // Метод, отвечающий за нажатие кнопки [Вверх] для левой панели
    public void leftBtnUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(leftFldPath.getText()).getParent();
        if (upperPath != null) {
            updateListLeft(upperPath);
        }
    }

    // Метод, отвечающий за нажатие кнопки [Вверх] для правой панели
    public void rightBtnUpAction(ActionEvent actionEvent) {
        RequestUpdateFileListMessage requestUpdateFileListMessage = new RequestUpdateFileListMessage();
        requestUpdateFileListMessage.setPath("");
        Client.startController.channelFuture.channel().writeAndFlush(requestUpdateFileListMessage);
    }

    // метод, отвечающий за работу левого Комбобокса
    public void leftComboAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateListLeft(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    // Обработка нажатия кнопки правой панели о создании новой папки
    public void rightBtnCreateDirAction(ActionEvent actionEvent) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Введите название новой папки");
        textInputDialog.setTitle("Создание новой папки");
        textInputDialog.showAndWait();
        if (textInputDialog.getResult() != null) {
            RequestCreateDirectoryMessage rcdm = new RequestCreateDirectoryMessage();
            rcdm.setNewDir(textInputDialog.getResult());
            Client.startController.channelFuture.channel().writeAndFlush(rcdm);
        }
    }

    // Обработка нажатия кнопки левой панели на удаление файла
    public void leftBtnDeleteAction(ActionEvent actionEvent) {
        if (leftTableView.isFocused() && leftTableView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы действительно хотите удалить?", ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Удаление файла");
            alert.setHeaderText("Будьте внимательны!");
            alert.showAndWait();
            if (alert.getResult().getText().equals("OK") ) {
                try {
                    Files.delete(Paths.get(leftFldPath.getText() + "\\" + leftTableView.getSelectionModel()
                            .getSelectedItem().getFileName()));
                    updateListLeft(Paths.get(leftFldPath.getText()));
                } catch (IOException e) {
                    outputMessage("Не удалось удалить файл или папку!");
                }
            }
        } else {
            outputMessage("Выберите файл или папку для удаления");
        }
    }

    // Обработка нажатия кнопки правой панели об удалении файла
    public void rightBtnDeleteFile(ActionEvent actionEvent) {
        if (rightTableView.isFocused() && rightTableView.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы действительно хотите удалить?", ButtonType.OK, ButtonType.CANCEL);
            alert.setTitle("Удаление файла");
            alert.setHeaderText("Будьте внимательны!");
            alert.showAndWait();
            if (alert.getResult().getText().equals("OK") ) {
                RequestDeleteFileMessage requestDeleteFileMessage = new RequestDeleteFileMessage();
                requestDeleteFileMessage.setFileName(rightTableView.getSelectionModel().getSelectedItem().getFileName());
                Client.startController.channelFuture.channel().writeAndFlush(requestDeleteFileMessage);
            }
        } else {
            outputMessage("Выберите файл или папку для удаления");
        }
    }

    // Метод выводит информационное окно на экран
    public void outputMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message,
                ButtonType.OK);
        alert.setHeaderText("Сообщение");
        alert.showAndWait();
    }
}
