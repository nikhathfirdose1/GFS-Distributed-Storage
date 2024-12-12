package com.example.chunkserver;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
            String senderUrl = System.getProperty("user.dir") + File.separator + "chunks" + File.separator + senderIp;

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
    public ResponseEntity<byte[]> getChunk(@RequestParam String chunkId) {
        byte[] data = chunkStorage.get(chunkId);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(data);
    }
}
