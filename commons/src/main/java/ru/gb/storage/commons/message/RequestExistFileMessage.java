package ru.gb.storage.commons.message;

import java.io.File;

public class RequestExistFileMessage extends Message{
    private String fileName;
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
