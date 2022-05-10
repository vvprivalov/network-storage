package ru.gb.storage.commons.message;

import java.util.List;

public class FileListMessage extends Message {
    private List<FileInfoMessage> listFile;

    public List<FileInfoMessage> getListFile() {

        return listFile;
    }

    public void setListFile(List<FileInfoMessage> listFile) {

        this.listFile = listFile;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (FileInfoMessage file : listFile) {
            stringBuilder.append(file.getFileName());
            stringBuilder.append( " " );
            stringBuilder.append(file.getTypeFile());
            stringBuilder.append(" ");
            stringBuilder.append(file.getFileSize());
            stringBuilder.append(" ");
            stringBuilder.append(file.getLastModified().toString());
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
