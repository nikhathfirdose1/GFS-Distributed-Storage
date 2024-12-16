package com.example.chunkserver.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

@Service
public class ChunkServerService {
    private String getBaseURL(String senderIp, int serverPort) {
        return System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort + File.separator
                + senderIp;
    }

    public String storeChunk(String senderIp, int serverPort, String chunkId, byte[] data) throws IOException, RuntimeException {
        String senderUrl = getBaseURL(senderIp, serverPort);

        File folder = new File(senderUrl);

        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory for sender: " + senderIp);
            }
        }
        String fileName = chunkId + ".chk";
        File chunkFile = new File(folder, fileName);

        FileOutputStream fos = new FileOutputStream(chunkFile);
        fos.write(data);
        fos.close();

        return chunkFile.getAbsolutePath();
    }

    public byte[] getChunk(String senderIp, int serverPort, String chunkId) throws IOException, RuntimeException{
        String baseDir = getBaseURL(senderIp, serverPort);
        File chunkFile = new File(baseDir + File.separator + chunkId + ".chk");

        if (!chunkFile.exists()) {
            throw new RuntimeException("Chunk not found: " + chunkId);
        }

        return Files.readAllBytes(chunkFile.toPath());
    }
}