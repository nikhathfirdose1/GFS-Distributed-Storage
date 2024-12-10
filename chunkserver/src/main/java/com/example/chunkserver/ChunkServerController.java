package com.example.chunkserver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    private final Map<String, byte[]> chunkStorage = new HashMap<>(); // Chunk ID to Data

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String chunkId, @RequestBody byte[] data) {
        chunkStorage.put(chunkId, data);
        return ResponseEntity.ok("Chunk stored: " + chunkId);
    }

    @GetMapping("/getChunk")
    public ResponseEntity<byte[]> getChunk(@RequestParam String chunkId) {
        byte[] data = chunkStorage.get(chunkId);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(data);
    }
}
