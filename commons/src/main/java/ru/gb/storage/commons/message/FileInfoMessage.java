package ru.gb.storage.commons.message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class FileInfoMessage extends Message {
    private String fileName;
    private long fileSize;
    private String typeFile;
    private String lastModified;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTypeFile() {
        return typeFile;
    }

    public void setTypeFile(String typeFile) {
        this.typeFile = typeFile;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public FileInfoMessage fillInfoFile(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.fileSize = Files.size(path);
            this.typeFile = Files.isDirectory(path) ? "DIR" : "FILE";
            if (typeFile.equals("DIR")) {
                fileSize = -1L;
                fileName = fileName.toUpperCase();
            } else {
                fileName = fileName.toLowerCase();
            }
            DateTimeFormatter dtfR = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                    ZoneOffset.ofHours(3)).format(dtfR);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл из указанного пути");
        }
        return this;
    }
}
