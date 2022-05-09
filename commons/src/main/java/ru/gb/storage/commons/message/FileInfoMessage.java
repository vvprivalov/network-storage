package ru.gb.storage.commons.message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfoMessage extends Message {
    private String fileName;
    private long fileSize;
    private String typeFile;
    private LocalDateTime lastModified;

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

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public FileInfoMessage(Path path) {
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
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(),
                    ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Ну удалось создать файл из указанного пути");
        }
    }
}