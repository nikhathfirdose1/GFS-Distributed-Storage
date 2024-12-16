package com.example.chunkmaster;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chunkMaster")
public class ChunkMasterController {

    // Record classes for request and response objects
    public record Chunk(String id, String content) {}

    // <fileName, <chunkNumber, List<chunkServers>>>
    private final Map<String, Map<String, List<String>>> fileToChunkNumMap = new HashMap<>();

    // List of chunk servers
    // TODO: Not hardcoding server instances. Let's dynamically populate it
    @Value("${chunk.servers}")
    private final List<String> serverList = new ArrayList<>();

    // Map of chunk server URLs and the number of chunks they hold
    private final Map<String, Integer> chunkServers = new HashMap<>();

    @PostMapping("/chunkStatus")
    public ResponseEntity<String> chunkServerStatus(@RequestBody Heartbeat heartbeat) {
        state.updateChunkServerState(heartbeat);
        return ResponseEntity.ok("STATUS_RECEIVED");
    }

    // Endpoint to map file chunks to chunk servers
    @PostMapping("/mapFile")
    public Map<String, List<String>> mapFile(@RequestParam("fileName") String fileName, @RequestBody List<Chunk> chunks, @RequestParam("numCopies") int numCopies) {
        // Check if file already exists in fileToChunkNumMap
        if (fileToChunkNumMap.containsKey(fileName)) {
            return fileToChunkNumMap.get(fileName);
        }

        // Ensure there are enough chunk servers to accommodate the requested copies
        if (chunkServers.size() < numCopies) {
            throw new IllegalArgumentException("Not enough chunk servers to store the required number of copies.");
        }

        // Create a new chunk server mapping for the file
        Map<String, List<String>> chunkNumToChunkServers = new LinkedHashMap<>();

        // Distribute file chunks across chunk servers
        for (int chunkNumber = 1; chunkNumber <= chunks.size(); chunkNumber++) {
            // Select `numCopies` servers with the least chunks
            List<String> selectedServers = getLeastLoadedServers(numCopies);

            // Update the chunk count for the selected servers
            for (String server : selectedServers) {
                chunkServers.put(server, chunkServers.get(server) + 1);
            }

            // Map the chunk to the selected servers
            chunkNumToChunkServers.put(chunks.get(chunkNumber - 1).id(), selectedServers);
        }

        // Store the mapping in fileToChunkNumMap
        fileToChunkNumMap.put(fileName, chunkNumToChunkServers);

        return chunkNumToChunkServers;
    }

    // Helper method to get the least-loaded servers
    private List<String> getLeastLoadedServers(int numCopies) {
        // Create a priority queue to sort servers by chunk count (min-heap)
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        queue.addAll(chunkServers.entrySet());

        List<String> selectedServers = new ArrayList<>();
        for (int i = 0; i < numCopies && !queue.isEmpty(); i++) {
            Map.Entry<String, Integer> entry = queue.poll();
            selectedServers.add(entry.getKey());
        }
    }

    // Endpoint to get all file-to-chunk-server mapping
    @GetMapping("/getAllFileMapping")
    public ResponseEntity<Map<String, Map<String, List<String>>>> getAllFileMapping() {
        Map<String, Map<String, List<String>>> mappings = state.allMapping();
        if (mappings.isEmpty()) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(mappings);
    }

    // Endpoint to get chunk servers for a file
    @GetMapping("/getFileMapping")
    public ResponseEntity<Response<FileMappingResponse, String>> getFileMapping(@RequestParam("fileName") String fileName) {
        Pair<Optional<FileMappingResponse>, Optional<Error>> pair = state.readFile(fileName);
        if (pair.first().isPresent()) {
            return ResponseEntity.ok(FileMappingResponse.successFulResponse(pair));
        } else {
            return ResponseEntity.badRequest().body(FileMappingResponse.errorResponse(pair));
        }
    }

    // Endpoint to get list of servers
    @GetMapping("/getServerList")
    public ResponseEntity<List<String>> getServers() {
        return ResponseEntity.ok(new ArrayList<>(state.chunkServersByNetworkAddress().keySet()));
    }

    // Endpoint to get chunk servers map
    @GetMapping("/getChunkServersMap")
    public ResponseEntity<Map<String, Integer>> getChunkServers() {
        return ResponseEntity.ok(chunkServers);
    }
}
