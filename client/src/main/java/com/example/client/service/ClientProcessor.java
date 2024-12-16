package com.example.client.service;

import com.example.client.entity.ChunkToChunkMaster;
import com.example.client.entity.ChunkToChunkServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientProcessor {

    public List<Object> split(byte[] fileData, int chunkSize, String fileName) {
        List<ChunkToChunkServer> chunkToChunkServers = new ArrayList<>();
        List<ChunkToChunkMaster> chunkToChunkMasters = new ArrayList<>();
        // Iterate through the byte array in chunks
        for (int i = 0; i < fileData.length; i += chunkSize) {
            int end = Math.min(i + chunkSize, fileData.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(fileData, i, chunk, 0, chunk.length);
            String uuid = UUID.randomUUID().toString();
            chunkToChunkMasters.add(new ChunkToChunkMaster(uuid, fileName, i+1));
            chunkToChunkServers.add(new ChunkToChunkServer(uuid, new String(chunk, StandardCharsets.UTF_8), fileName, i+1));
        }
        return Arrays.asList(chunkToChunkMasters, chunkToChunkServers);
    }

    public byte[] merge(List<ChunkToChunkServer> fileChunks) {

        fileChunks.sort(Comparator.comparingInt(chunk->chunk.getOrder()));

        StringBuilder stringBuilder = new StringBuilder();

        // Append content after validating
        for (ChunkToChunkServer chunk : fileChunks) {
            stringBuilder.append(chunk.getContent());
            // if (chunk.getContent() != null && !chunk.getContent().isEmpty()) {
            //     stringBuilder.append(new String(Base64.getDecoder().decode(chunk.getContent()), StandardCharsets.UTF_8));
            // }
        }

        return stringBuilder.toString().getBytes();
    }
}