package ru.gb.storage.commons.message;

public class RequestCreateDirectoryMessage extends Message{
    private String newDir;

    public String getNewDir() {
        return newDir;
    }

    public void setNewDir(String newDir) {
        this.newDir = newDir;
    }
}
