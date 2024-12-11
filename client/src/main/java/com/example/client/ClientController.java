package com.example.client;

import com.example.client.entity.Chunk;
import com.example.client.entity.request.WriteToChunkRequest;
import com.example.client.entity.response.MapFileResponse;
import com.example.client.service.ClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private ClientService clientService;
    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam String fileName, @RequestBody byte[] fileData, @RequestParam int numCopies) {

        List<Chunk> chunks = clientService.split(fileData);
        // Notify ChunkMaster to store the file
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8080/chunkmaster/mapFile?fileName=" + fileName + "&numCopies=" + numCopies,
                chunks,
                String.class
        );

        if (response.getStatusCode().isError()) {
            return ResponseEntity.status(response.getStatusCode()).body("Failed to store file.");
        }

        MapFileResponse chunkToChunkServerMap = new MapFileResponse();
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
                throw new RuntimeException("chunkId not found in response of mapFile");
            }
            List<String> chunkAddress = chunkToChunkServerMap.getChunkAddressMap().get(chunk.getId());
            WriteToChunkRequest request = new WriteToChunkRequest(chunk, chunkAddress);
            restTemplate.postForEntity("http://localhost:8080/chunkserver/storeChunk?fileName=" + fileName, request, String.class);
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
//                restTemplate.postForEntity(server + "/chunkserver/storeChunk?chunkId=" + chunk, fileData, String.class);
//            }
//        }
        return ResponseEntity.ok("File uploaded and chunks distributed.");
    }
}