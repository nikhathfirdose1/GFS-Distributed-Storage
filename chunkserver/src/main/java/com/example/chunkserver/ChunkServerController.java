package com.example.chunkserver;

import com.example.chunkserver.entity.Chunk;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    @Autowired
    private ChunkServerService chunkServerService;
    private Map<String, List<String>> chunkStorage = new HashMap<>();

    @PostConstruct
    public void initializeChunkStorage() {
        System.out.println("Initializing chunk storage...");
        // chunkStorage = chunkServerService.retrieveChunks();
        System.out.println("Chunk storage initialized: " + chunkStorage);
    }

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String filename, @RequestBody Chunk chunk,
            HttpServletRequest request) {
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            System.out.println("Received request to store chunk: " + chunk.getId() + ", for file: " + filename);
            List<String> chunkIds = chunkStorage.containsKey(filename) ? chunkStorage.get(filename) : new ArrayList<>();
            chunkIds.stream().filter(c -> c.equals(chunk.getId()))
                    .findFirst()
                    .ifPresentOrElse(null, () -> chunkIds.add(chunk.getId()));
            chunkStorage.put(filename, chunkIds);

            chunkServerService.storeChunk(senderIp, serverPort, filename, chunk);

            return ResponseEntity.ok("Stored ChunkID: " + chunk.getId() + ", for File: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing chunk to file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/getChunk")
    public ResponseEntity<Chunk> getChunk(@RequestParam String filename, String chunkId, HttpServletRequest request) {
        if (!chunkStorage.containsKey(filename) || !chunkStorage.get(filename).contains(chunkId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();
            System.out.println("Received request to retrieve chunk: " + chunkId + ", for file: " + filename);
            String data = chunkServerService.getChunk(senderIp, serverPort, filename, chunkId);

            return ResponseEntity.ok(new Chunk(chunkId, data));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
