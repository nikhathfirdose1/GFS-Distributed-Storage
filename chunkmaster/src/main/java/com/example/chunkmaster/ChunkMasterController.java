package com.example.chunkmaster;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chunkmaster")
public class ChunkMasterController {

    private final Map<String, List<String>> fileChunkMapping = new HashMap<>(); // File to Chunks
    private final Map<String, List<String>> chunkServerMapping = new HashMap<>(); // Chunk to ChunkServers
    private final List<String> chunkServers = Arrays.asList("http://localhost:8081", "http://localhost:8082"); // ChunkServers

    @PostMapping("/storeFile")
    public ResponseEntity<String> storeFile(@RequestParam String fileName, @RequestParam int numCopies) {
        // Simulate file splitting into chunks
        List<String> chunks = List.of(fileName + "_chunk1", fileName + "_chunk2");
        fileChunkMapping.put(fileName, chunks);

        // Assign chunks to servers
        for (String chunk : chunks) {
            List<String> assignedServers = new ArrayList<>();
            for (int i = 0; i < numCopies; i++) {
                String server = chunkServers.get((chunk.hashCode() + i) % chunkServers.size());
                if (!assignedServers.contains(server)) {
                    assignedServers.add(server);
                }
            }
            chunkServerMapping.put(chunk, assignedServers);
        }

        return ResponseEntity.ok("File stored successfully with chunks distributed.");
    }

    @GetMapping("/fileChunks")
    public ResponseEntity<Map<String, List<String>>> getFileChunks() {
        return ResponseEntity.ok(fileChunkMapping);
    }

    @GetMapping("/chunkServers")
    public ResponseEntity<Map<String, List<String>>> getChunkServerMapping() {
        return ResponseEntity.ok(chunkServerMapping);
    }
}