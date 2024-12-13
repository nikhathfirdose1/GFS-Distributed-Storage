package com.example.chunkserver;

import com.example.chunkserver.entity.Chunk;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    private final Map<String, List<Chunk>> chunkStorage = new HashMap<>();

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String filename, @RequestBody Chunk chunk) {
        System.out.println("Received request to store chunk: " + chunk.getId() + ", for file: " + filename);
        List<Chunk> chunks = chunkStorage.containsKey(filename) ? chunkStorage.get(filename) : new ArrayList<>();

        chunks.add(chunk);
        chunkStorage.put(filename, chunks);
        System.out.println("Stored ChunkID: " + chunk.getId() + ", for File: " + filename);
        System.out.println("Current Chunks stored for file: "+filename);
        for (Chunk c : chunks){
            System.out.println(c.getId());
        }
        return ResponseEntity.ok("Stored ChunkID: " + chunk.getId() + ", for File: " + filename);
    }

    @GetMapping("/getChunk")
    public ResponseEntity<Chunk> getChunk(@RequestParam String filename, String chunkId) {
        System.out.println("Received request to retrieve chunk: " + chunkId + ", for file: " + filename);

        if (!chunkStorage.containsKey(filename)) {
            System.out.println("File not found with filename: " + filename);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Chunk> chunks = chunkStorage.get(filename);
        for (Chunk chunk : chunks) {
            if (chunk.getId().equals(chunkId)) {
                System.out.println("Chunk found with chunkId: " + chunkId + ", for file: " + filename);
                return ResponseEntity.ok(chunk);
            }
        }

        System.out.println("Chunk not found with chunkId: " + chunkId + ", for file: " + filename);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
