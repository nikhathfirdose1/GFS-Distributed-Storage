package com.example.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam String fileName, @RequestBody byte[] fileData, @RequestParam int numCopies) {
        // Notify ChunkMaster to store the file
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8080/chunkmaster/storeFile?fileName=" + fileName + "&numCopies=" + numCopies,
                null,
                String.class
        );

        if (response.getStatusCode().isError()) {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to store file.");
        }

        // Simulate splitting the file into chunks and sending to ChunkServers
        String[] chunks = {fileName + "_chunk1", fileName + "_chunk2"};
        for (String chunk : chunks) {
            // Get servers for the chunk from ChunkMaster
            ResponseEntity<Map> mappingResponse = restTemplate.getForEntity(
                    "http://localhost:8080/chunkmaster/chunkServers",
                    Map.class
            );
            Map<String, List<String>> chunkServerMapping = mappingResponse.getBody();

            for (String server : chunkServerMapping.get(chunk)) {
                restTemplate.postForEntity(server + "/chunkserver/storeChunk?chunkId=" + chunk, fileData, String.class);
            }
        }

        return ResponseEntity.ok("File uploaded and chunks distributed.");
    }
}