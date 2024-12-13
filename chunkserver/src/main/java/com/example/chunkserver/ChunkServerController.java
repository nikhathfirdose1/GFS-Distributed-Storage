package com.example.chunkserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    @Autowired private ChunkServerService chunkServerService;

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String chunkId, @RequestBody byte[] data,
            HttpServletRequest request) {
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            String absolutePath = chunkServerService.storeChunk(senderIp, serverPort, chunkId, data);

            return ResponseEntity.ok("Chunk stored successfully: " + absolutePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing chunk: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/getChunk")
    public ResponseEntity<byte[]> getChunk(@RequestParam String chunkId, HttpServletRequest request) {
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            byte[] data = chunkServerService.getChunk(senderIp, serverPort, chunkId);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error retrieving chunk: " + e.getMessage()).getBytes());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
