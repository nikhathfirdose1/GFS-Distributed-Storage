package com.example.chunkserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import com.example.chunkserver.entity.Chunk;

@Service
public class ChunkServerService {
    private String getBaseURL(int serverPort, String fileName) {
        return System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort + File.separator + fileName;
    }

    public String storeChunk(int serverPort, String filename, Chunk chunk)
            throws IOException, RuntimeException {
        File directory = new File(getBaseURL(serverPort, filename));
        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            if (!dirCreated) {
                throw new RuntimeException("Failed to create directory: " + directory.getAbsolutePath());
            }
        }

        File chunkFile = new File(directory, chunk.getId() + ".chk");
        try (FileWriter writer = new FileWriter(chunkFile)) {
            writer.write(chunk.getContent());
        } catch (IOException e) {
            throw new IOException(chunkFile.getAbsolutePath());
        }

        return chunkFile.getAbsolutePath();
    }

    public String getChunk(int serverPort, String filename, String chunkId)
            throws IOException, RuntimeException {

        File directory = new File(getBaseURL(serverPort, filename));
        if (!directory.exists()) {
            throw new RuntimeException("File chunks not found: " + directory.getAbsolutePath());
        }

        File chunkFile = new File(directory + File.separator + chunkId + ".chk");
        if (!chunkFile.exists()) {
            throw new IOException("Chunk not found: " + chunkId + ".chk");
        }

        return Files.readString(chunkFile.toPath());
    }
}
