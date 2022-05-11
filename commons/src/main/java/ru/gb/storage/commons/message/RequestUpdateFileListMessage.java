package ru.gb.storage.commons.message;

public class RequestUpdateFileListMessage extends Message{
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
