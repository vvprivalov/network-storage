package ru.gb.storage.commons.message;

import java.io.File;

public class AnswerExistFileMessage extends Message{
    private boolean exist;
    private String fileName;
    private File file;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
