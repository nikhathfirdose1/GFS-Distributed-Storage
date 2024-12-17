package com.example.client.service;

import com.example.client.entity.ChunkToChunkMaster;
import com.example.client.entity.ChunkToChunkServer;
import com.example.client.entity.response.MasterWriteResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.io.ByteArrayOutputStream;

import java.util.*;

import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;

public class ClientService {
    private final String MASTER = "http://192.168.10.1:8080";

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
            System.out.println(chunkToChunkServers.get(0).getContent());
            // Notify ChunkMaster to store the file
            ResponseEntity<String> response = restTemplate.postForEntity(
                    MASTER+"/chunkMaster/mapFile?fileName=" + fileName + "&numCopies=" + numCopies,
                    chunkToChunkMasters,
                    String.class
            );


            if (response.getStatusCode().isError()) {
                return ResponseEntity.status(response.getStatusCode()).body("Failed to store file.");
            }

            MasterWriteResponse masterWriteResponse;
            try {
                masterWriteResponse = objectMapper.readValue(response.getBody(), MasterWriteResponse.class);
                if (!masterWriteResponse.getSuccess() && masterWriteResponse.getError() != null) {
                    throw new RuntimeException(masterWriteResponse.getError());
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            if (chunkToChunkServers.size() != masterWriteResponse.getData().size()) {
                throw new RuntimeException("Chunk size in master does not match the chunk size split in client");
            }
            Map<String, List<String>> chunkToChunkServerMap = masterWriteResponse.getData();
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
                    String chunkServerStoreChunkURL = "http://" + chunkAddress + "/chunkserver/storeChunk";
                    restTemplate.exchange(
                            chunkServerStoreChunkURL,
                            HttpMethod.POST,
                            entity,
                            String.class
                    );
                }
            }
            return ResponseEntity.ok("File uploaded and chunks distributed.");
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.ok("An Unexpected Error Occurred!");
        }
    }


    public ResponseEntity<byte[]> readFile(String fileName) {
        // Get the chunk-to-server mapping from the ChunkMaster
        ResponseEntity<String> response = restTemplate.getForEntity(
                MASTER+"/chunkMaster/getFileMapping?fileName=" + fileName,
                String.class
        );

        if (response.getStatusCode().isError()) {
            return ResponseEntity.status(response.getStatusCode()).body(null);
        }

        MasterWriteResponse masterWriteResponse;
        try {
            masterWriteResponse = objectMapper.readValue(response.getBody(), MasterWriteResponse.class);
            if (!masterWriteResponse.getSuccess() && masterWriteResponse.getError() != null) {
                throw new RuntimeException(masterWriteResponse.getError());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Map<String, List<String>> chunkToServerMap = masterWriteResponse.getData();

        // List<ChunkToChunkServer> fileChunks = new ArrayList<>();

        // for (Map.Entry<String, List<String>> entry : chunkToServerMap.entrySet()) {
        //     String chunkId = entry.getKey();
        //     List<String> serverUrls = entry.getValue();

        //     ChunkToChunkServer chunkData = null;
        //     boolean isChunkRetrieved = false;

        //     for (String serverUrl : serverUrls) {
        //         String chunkServerRetrieveChunkURL = "http://" + serverUrl + "/chunkserver/getChunk?chunkId=" + chunkId + "&filename=" + fileName;

        //         try {
        //             ResponseEntity<ChunkToChunkServer> chunkResponse = restTemplate.getForEntity(
        //                     chunkServerRetrieveChunkURL,
        //                     ChunkToChunkServer.class
        //             );

        //             if (chunkResponse.getStatusCode().is2xxSuccessful()) {
        //                 chunkData = chunkResponse.getBody();
        //                 isChunkRetrieved = true;
        //                 break; // Exit the loop as we successfully retrieved the chunk
        //             }
        //         } catch (Exception e) {
        //             System.out.println("Failed to retrieve chunk from server: " + serverUrl + " - Trying next server.");
        //         }
        //     }

        //     if (!isChunkRetrieved) {
        //         throw new RuntimeException("Failed to retrieve chunk: " + chunkId + " from all available servers.");
        //     }
            
        //     fileChunks.add(chunkData);
        // }

        byte[] fileData = clientProcessor.merge(retrieveChunksParallel(chunkToServerMap, fileName));


        return ResponseEntity.ok(fileData);
    }

    
    public List<ChunkToChunkServer> retrieveChunksParallel(Map<String, List<String>> chunkToServerMap, String fileName) {
        List<CompletableFuture<ChunkToChunkServer>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10); // Define a thread pool size
    
        for (Map.Entry<String, List<String>> entry : chunkToServerMap.entrySet()) {
            String chunkId = entry.getKey();
            List<String> serverUrls = entry.getValue();
    
            // Submit chunk retrieval tasks as CompletableFutures
            CompletableFuture<ChunkToChunkServer> chunkFuture = CompletableFuture.supplyAsync(() -> {
                for (String serverUrl : serverUrls) {
                    String chunkServerRetrieveChunkURL = "http://" + serverUrl + "/chunkserver/getChunk?chunkId=" + chunkId + "&filename=" + fileName;
                    try {
                        ResponseEntity<ChunkToChunkServer> chunkResponse = restTemplate.getForEntity(
                                chunkServerRetrieveChunkURL,
                                ChunkToChunkServer.class
                        );
                        if (chunkResponse.getStatusCode().is2xxSuccessful()) {
                            return chunkResponse.getBody();
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to retrieve chunk from server: " + serverUrl + " - Trying next server.");
                    }
                }
                throw new RuntimeException("Failed to retrieve chunk: " + chunkId + " from all available servers.");
            }, executor);
    
            futures.add(chunkFuture);
        }
    
        // Collect and process results
        try {
            List<ChunkToChunkServer> fileChunks = futures.stream()
                    .map(CompletableFuture::join) // Wait for all futures to complete
                    .collect(Collectors.toList());
    
            System.out.println("Successfully retrieved all chunks!");
            // Process fileChunks as needed
            return fileChunks;
        } catch (CompletionException e) {
            System.err.println("Error during chunk retrieval: " + e.getMessage());
            throw e;
        } finally {
            executor.shutdown(); // Shutdown the executor service
        }
    }
    
}