package com.example.client.service;

import com.example.client.entity.Chunk;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ClientProcessor {

    public List<Chunk> split(byte[] fileData, int chunkSize) {
        List<Chunk> chunks = new ArrayList<>();
        // Iterate through the byte array in chunks
        for (int i = 0; i < fileData.length; i += chunkSize) {
            int end = Math.min(i + chunkSize, fileData.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(fileData, i, chunk, 0, chunk.length);
            chunks.add(new Chunk(UUID.randomUUID().toString(), new String(chunk, StandardCharsets.UTF_8)));
        }
        return chunks;
    }

    public byte[] merge(String[] chunks) {
        List<Byte> byteList = new ArrayList<>();

        for (String base64Str : chunks) {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
            for (byte b : decodedBytes) {
                byteList.add(b);
            }
        }

        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }

        return byteArray;
        //converting byteArray back to file format and saving it
//        try {
//            // Use Files.write to save the byte array to the file
//            Files.write(Paths.get(filePath), byteArray);
//        } catch (IOException e) {
//            System.err.println("Error saving file: " + e.getMessage());
//        }
    }
}