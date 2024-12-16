package com.example.chunkserver;

import com.example.chunkserver.entity.Chunk;
import com.example.chunkserver.entity.ChunkMetadata;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    @Autowired
    private ChunkServerService chunkServerService;
    @Autowired
    private HeartBeatService heartBeatService;
    private Set<ChunkMetadata> storedChunkMetadataSet;

    @Value("${server.port}")
    private int serverPort;

    @Value("${heartbeat.timer}")
    private int heartbeatTimer;

    @Value("${server.ip}")
    String chunkServerIp;
    String chunkServerAddress;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @PostConstruct
    public void initializeChunkStorage() {
        System.out.println("Initializing chunk storage...");
        chunkServerAddress = chunkServerIp + ":" + serverPort;

        File directory = new File(System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort);
        File[] directories = directory.listFiles(File::isDirectory);

        storedChunkMetadataSet = new HashSet<>();
        /*if (directories != null) {
            for (File dir : directories) {
                File[] files = dir.listFiles(File::isFile);
                if (files != null) {
                    for (File file : files) {
                        String chunkId = file.getName().replace(".chk", "");
                        storedChunkMetadataSet.add(new ChunkMetadata(chunkId, dir.getName(), -1));
                    }
                }
            }
        }*/
        scheduler.scheduleAtFixedRate(() -> {
            heartBeatService.sendHeartBeatToMaster(chunkServerAddress, storedChunkMetadataSet);   //periodic heartbeat
        }, 0, heartbeatTimer, TimeUnit.MILLISECONDS);
    }

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String filename, @RequestBody Chunk chunk) {
        try {
            System.out.println("Received request to store chunk: " + chunk.getId() + ", for file: " + filename);
            chunkServerService.storeChunk(serverPort, filename, chunk);

            storedChunkMetadataSet.add(new ChunkMetadata(chunk.getId(), filename, chunk.getOrder()));

            chunkServerService.storeChunk(serverPort, filename, chunk);
            heartBeatService.sendHeartBeatToMaster(chunkServerAddress, storedChunkMetadataSet);

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
        int order = -1;

        for (ChunkMetadata chunkMetadata : storedChunkMetadataSet) {
            if (chunkMetadata.getId().equals(chunkId)) {
                order = chunkMetadata.getOrder();
                break;
            }
        }
        if (order == -1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            System.out.println("Received request to retrieve chunk: " + chunkId + ", for file: " + filename);
            String data = chunkServerService.getChunk(serverPort, filename, chunkId);

            return ResponseEntity.ok(new Chunk(chunkId, data, order));
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
