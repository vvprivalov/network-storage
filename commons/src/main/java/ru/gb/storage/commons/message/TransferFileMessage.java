package ru.gb.storage.commons.message;

public class TransferFileMessage extends Message{
    private byte[] content;
    private long startPosition;
    private boolean last;
    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }

    public long getStartPosition() {

        return startPosition;
    }

    public void setStartPosition(long startPosition) {

        this.startPosition = startPosition;
    }
}
