package com.example.chunkserver;

import com.example.chunkserver.entity.Chunk;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    @Autowired
    private ChunkServerService chunkServerService;
    private Map<String, List<String>> chunkStorage = new HashMap<>();

    @Value("${server.port}")
    private int serverPort;

    @PostConstruct
    public void initializeChunkStorage() {
        System.out.println("Initializing chunk storage...");
        // chunkStorage = chunkServerService.retrieveChunks();
        File directory = new File(System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort);
        File[] directories = directory.listFiles(File::isDirectory);

        if (directories != null) {
            for (File dir : directories) {
                File[] files = dir.listFiles(File::isFile);
                if (files != null) {
                    List<String> chunkIds = new ArrayList<>();
                    for (File file : files) {
                        chunkIds.add(file.getName().replace(".chk", ""));
                    }
                    chunkStorage.put(dir.getName(), chunkIds);
                }
            }
        }
        System.out.println("Chunk storage initialized: " + chunkStorage);
    }

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String filename, @RequestBody Chunk chunk) {
        try {
            System.out.println("Received request to store chunk: " + chunk.getId() + ", for file: " + filename);
            chunkServerService.storeChunk(serverPort, filename, chunk);

            List<String> chunkIds = chunkStorage.containsKey(filename) ? chunkStorage.get(filename) : new ArrayList<>();
            chunkIds.stream().filter(c -> c.equals(chunk.getId()))
                    .findFirst()
                    .ifPresentOrElse(null, () -> chunkIds.add(chunk.getId()));
            chunkStorage.put(filename, chunkIds);

            chunkServerService.storeChunk(serverPort, filename, chunk);

            return ResponseEntity.ok("Stored ChunkID: " + chunk.getId() + ", for File: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing chunk to file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getChunk")
    public ResponseEntity<Chunk> getChunk(@RequestParam String filename, String chunkId) {
        if (!chunkStorage.containsKey(filename) || !chunkStorage.get(filename).contains(chunkId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            System.out.println("Received request to retrieve chunk: " + chunkId + ", for file: " + filename);
            String data = chunkServerService.getChunk(serverPort, filename, chunkId);

            return ResponseEntity.ok(new Chunk(chunkId, data));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
