package com.example.chunkserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chunkserver")
public class ChunkServerController {

    private final Map<String, byte[]> chunkStorage = new HashMap<>(); // Chunk ID to Data

    @PostMapping("/storeChunk")
    public ResponseEntity<String> storeChunk(@RequestParam String chunkId, @RequestBody byte[] data,
            HttpServletRequest request) {
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            String senderUrl = System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort
                    + File.separator + senderIp;

            File folder = new File(senderUrl);

            if (!folder.exists()) {
                boolean created = folder.mkdirs();
                if (!created) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create directory for sender: " + senderIp);
                }
            }

            String fileName = chunkId + ".chk";
            File chunkFile = new File(folder, fileName);

            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                fos.write(data);
            }

            return ResponseEntity.ok("Chunk stored successfully: " + chunkFile.getAbsolutePath());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error storing chunk: " + e.getMessage());
        }
    }

    @GetMapping("/getChunk")
    public ResponseEntity<byte[]> getChunk(@RequestParam String chunkId, HttpServletRequest request) {
        try {
            String senderIp = request.getRemoteAddr();
            int serverPort = request.getLocalPort();

            String baseDir = System.getProperty("user.dir") + File.separator + "chunks" + File.separator + serverPort;
            File chunkFile = new File(baseDir + File.separator + senderIp + File.separator + chunkId + ".chk");

            if (!chunkFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(("Chunk not found: " + chunkId).getBytes());
            }

            byte[] data = Files.readAllBytes(chunkFile.toPath());
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error retrieving chunk: " + e.getMessage()).getBytes());
        }
    }
}
