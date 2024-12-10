package com.example.chunkmaster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;

import java.util.*;

@RestController
@RequestMapping("/chunkMaster")
public class ChunkMasterController {

    // <fileName, <chunkNumber, List<chunkServers>>>
    private static final Map<String, Map<Integer, List<String>>> fileToChunkNumMap = new HashMap<>();

    // List of chunk servers
    @Value("${chunk.servers}")
    private final List<String> serverList = new ArrayList<>();

    // Map of chunk server URLs and the number of chunks they hold
    private final Map<String, Integer> chunkServers = new HashMap<>();

    // Load the initial list of servers into the chunkServers map
    @PostConstruct
    public void initializeChunkServers() {
        for (String server : serverList) {
            if (!chunkServers.containsKey(server)) {
                chunkServers.put(server, 0); // Initialize with 0 chunks
                System.out.println("Loaded server: " + server);
            }
        }
    }

    // Endpoint to map file chunks to chunk servers
    @PostMapping("/mapFile")
    public Map<Integer, List<String>> mapFile(@RequestParam("fileName") String fileName, @RequestParam("numChunks") int numChunks, @RequestParam("numCopies") int numCopies) {
        // Check if file already exists in fileToChunkNumMap
        if (fileToChunkNumMap.containsKey(fileName)) {
            return fileToChunkNumMap.get(fileName);
        }

        // Ensure there are enough chunk servers to accommodate the requested copies
        if (chunkServers.size() < numCopies) {
            throw new IllegalArgumentException("Not enough chunk servers to store the required number of copies.");
        }

        // Create a new chunk server mapping for the file
        Map<Integer, List<String>> chunkNumToChunkServers = new LinkedHashMap<>();

        // Distribute file chunks across chunk servers
        for (int chunkNumber = 1; chunkNumber <= numChunks; chunkNumber++) {
            // Select `numCopies` servers with the least chunks
            List<String> selectedServers = getLeastLoadedServers(numCopies);

            // Update the chunk count for the selected servers
            for (String server : selectedServers) {
                chunkServers.put(server, chunkServers.get(server) + 1);
            }

            // Map the chunk to the selected servers
            chunkNumToChunkServers.put(chunkNumber, selectedServers);
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

        return selectedServers;
    }

    // Endpoint to get all file-to-chunk-server mapping
    @GetMapping("/getAllFileMapping")
    public ResponseEntity<Map<String, Map<Integer, List<String>>>> getAllFileMapping() {
        if (fileToChunkNumMap.isEmpty()) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(fileToChunkNumMap);
    }

    // Endpoint to get chunk servers for a file
    @GetMapping("/getFileMapping")
    public ResponseEntity<Map<Integer, List<String>>> getFileMapping(@RequestParam("fileName") String fileName) {
        if (!fileToChunkNumMap.containsKey(fileName)) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(fileToChunkNumMap.get(fileName));
    }

    // Endpoint to get list of servers
    @GetMapping("/getServerList")
    public ResponseEntity<List<String>> getServers() {
        return ResponseEntity.ok(serverList);
    }

    // Endpoint to add a chunk server
    @PostMapping("/addChunkServer")
    public ResponseEntity<String> addChunkServer(@RequestParam("chunkServerUrl") String chunkServerUrl) {
        if (!chunkServers.containsKey(chunkServerUrl)) {
            System.out.println("Adding chunk server: " + chunkServerUrl);
            serverList.add(chunkServerUrl);
            chunkServers.put(chunkServerUrl, 0); // Initialize with 0 chunks
        }
        return ResponseEntity.ok("Chunk server added successfully.");
    }

    // Endpoint to get chunk servers map
    @GetMapping("/getChunkServersMap")
    public ResponseEntity<Map<String, Integer>> getChunkServers() {
        return ResponseEntity.ok(chunkServers);
    }
}
