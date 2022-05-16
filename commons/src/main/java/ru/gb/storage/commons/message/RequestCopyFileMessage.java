package ru.gb.storage.commons.message;

public class RequestCopyFileMessage extends Message{
    private String fileName;
    private boolean exist;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }
}
