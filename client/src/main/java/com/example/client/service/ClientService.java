package com.example.client.service;

import com.example.client.entity.ChunkToChunkMaster;
import com.example.client.entity.ChunkToChunkServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;

import java.util.*;

public class ClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ClientProcessor clientProcessor;

    public ClientService() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        clientProcessor = new ClientProcessor();
    }

    public ResponseEntity<String> uploadFile(@RequestParam String fileName, @RequestBody byte[] fileData, @RequestParam int numCopies) {
        try {
            int chunkSizeInBytes = 1000;
            List<Object> splitData = clientProcessor.split(fileData, chunkSizeInBytes, fileName);
            List<ChunkToChunkMaster> chunkToChunkMasters = (List<ChunkToChunkMaster>) splitData.get(0);
            List<ChunkToChunkServer> chunkToChunkServers = (List<ChunkToChunkServer>) splitData.get(1);
            // Notify ChunkMaster to store the file
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:8080/chunkMaster/mapFile?fileName=" + fileName + "&numCopies=" + numCopies,
                    chunkToChunkMasters,
                    String.class
            );


            if (response.getStatusCode().isError()) {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to store file.");
            }

            Map<String, List<String>> chunkToChunkServerMap;
            try {
                chunkToChunkServerMap = objectMapper.readValue(response.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            if (chunkToChunkServers.size() != chunkToChunkServerMap.size()) {
                throw new RuntimeException("Chunk size in master does not match the chunk size split in client");
            }

            //send chunks to respective chunk server addresses
            for (ChunkToChunkServer chunkToChunkServer : chunkToChunkServers) {
                if (!chunkToChunkServerMap.containsKey(chunkToChunkServer.getId())) {
                    throw new RuntimeException("chunkId not found in response from master");
                }
                List<String> chunkAddressList = chunkToChunkServerMap.get(chunkToChunkServer.getId());
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                for (String chunkAddress : chunkAddressList) {
                    HttpEntity<ChunkToChunkServer> entity = new HttpEntity<>(chunkToChunkServer, headers);
                    String chunkServerStoreChunkURL = chunkAddress + "/chunkserver/storeChunk";
                    restTemplate.exchange(
                            chunkServerStoreChunkURL,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );
                }
            }


//        // Simulate splitting the file into chunks and sending to ChunkServers
//        String[] chunks = {fileName + "_chunk1", fileName + "_chunk2"};
//        for (String chunk : chunks) {
//            // Get servers for the chunk from ChunkMaster
//            ResponseEntity<Map> mappingResponse = restTemplate.getForEntity(
//                    "http://localhost:8080/chunkmaster/chunkServers",
//                    Map.class
//            );
//            Map<String, List<String>> chunkServerMapping = mappingResponse.getBody();
//
//            for (String server : chunkServerMapping.get(chunk)) {
//                restTemplate.postForEntity(server + "/chunkServer/storeChunk?chunkId=" + chunk, fileData, String.class);
//            }
//        }
            return ResponseEntity.ok("File uploaded and chunks distributed.");
        }catch(Exception e){
            System.out.println(e);
            return ResponseEntity.ok("An Unexpected Error Occurred!");
        }
    }


    public ResponseEntity<byte[]> readFile(String fileName) {
        // Get the chunk-to-server mapping from the ChunkMaster
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:8080/chunkMaster/getFileMapping?fileName=" + fileName,
                String.class
        );

        if (response.getStatusCode().isError()) {
            return ResponseEntity.status(response.getStatusCode()).body(null);
        }

        Map<String, List<String>> chunkToServerMap;
        try {
            chunkToServerMap = objectMapper.readValue(response.getBody(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<ChunkToChunkServer> fileChunks = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : chunkToServerMap.entrySet()) {
            String chunkId = entry.getKey();
            List<String> serverUrls = entry.getValue();

            ChunkToChunkServer chunkData = null;
            boolean isChunkRetrieved = false;

            for (String serverUrl : serverUrls) {
                String chunkServerRetrieveChunkURL = serverUrl + "/chunkserver/getChunk?chunkId=" + chunkId + "&filename=" + fileName;

                try {
                    ResponseEntity<ChunkToChunkServer> chunkResponse = restTemplate.getForEntity(
                            chunkServerRetrieveChunkURL,
                            ChunkToChunkServer.class
                    );

                    if (chunkResponse.getStatusCode().is2xxSuccessful()) {
                        chunkData = chunkResponse.getBody();
                        isChunkRetrieved = true;
                        break; // Exit the loop as we successfully retrieved the chunk
                    }
                } catch (Exception e) {
                    System.out.println("Failed to retrieve chunk from server: " + serverUrl + " - Trying next server.");
                }
            }

            if (!isChunkRetrieved) {
                throw new RuntimeException("Failed to retrieve chunk: " + chunkId + " from all available servers.");
            }
            
            fileChunks.add(chunkData);
        }

        byte[] fileData = clientProcessor.merge(fileChunks);

        
        return ResponseEntity.ok(fileData);
    }
}