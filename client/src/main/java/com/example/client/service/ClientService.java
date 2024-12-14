package com.example.client.service;

import com.example.client.entity.Chunk;
import com.example.client.entity.response.MapFileResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ClientService {

    @Autowired
    private ClientProcessor clientProcessor;
    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResponseEntity<String> uploadFile(@RequestParam String fileName, @RequestBody byte[] fileData, @RequestParam int numCopies) {
        int chunkSizeInBytes = 1000;
        List<Chunk> chunks = clientProcessor.split(fileData, chunkSizeInBytes);
        // Notify ChunkMaster to store the file
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8080/chunkmaster/mapFile?fileName=" + fileName + "&numCopies=" + numCopies,
                chunks,
                String.class
        );

        if (response.getStatusCode().isError()) {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to store file.");
        }

        MapFileResponse chunkToChunkServerMap;
        try {
             chunkToChunkServerMap = objectMapper.readValue(response.getBody(), MapFileResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        if(chunks.size()!=chunkToChunkServerMap.getChunkAddressMap().size()){
            throw new RuntimeException("Chunk size in master does not match the chunk size split in client");
        }

        //send chunks to respective chunk server addresses
        for(Chunk chunk: chunks){
            if(!chunkToChunkServerMap.getChunkAddressMap().containsKey(chunk.getId())){
                throw new RuntimeException("chunkId not found in response from master");
            }
            List<String> chunkAddressList = chunkToChunkServerMap.getChunkAddressMap().get(chunk.getId());
            for(String chunkAddress: chunkAddressList){
                restTemplate.postForEntity(chunkAddress + "/chunkServer/storeChunk?fileName=" + fileName, chunk, String.class);
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
    }
}