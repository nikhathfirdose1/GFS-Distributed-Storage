package com.example.chunkserver.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    private final Map<String, byte[]> chunkStorage = new HashMap<>(); // Chunk ID to Data
    @Autowired private ChunkServerService chunkServerService;

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String filename, @RequestParam String chunk_id, @RequestBody byte[] data, HttpServletRequest request) {
        System.out.println("Received request to store chunk: " + chunk_id + ", for file: " + filename);
        String chunk_name = filename + "-" + chunk_id;
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            String absolutePath = chunkServerService.storeChunk(senderIp, serverPort, chunk_name, data);

            return ResponseEntity.ok("Stored ChunkID: " + chunk_id + ", for File: " + filename);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing chunk: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
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