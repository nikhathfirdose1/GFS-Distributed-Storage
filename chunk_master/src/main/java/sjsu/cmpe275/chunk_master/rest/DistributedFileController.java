package sjsu.cmpe275.chunk_master.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/files")
public class DistributedFileController {

    // <fileName, <chunkNumber, List<chunkServers>>>
    private static final Map<String, Map<Integer, List<String>>> fileToChunkNumMap = new HashMap<>();
    
    // List of chunk server URLs (defined in application.properties)
    @Value("${chunk.servers}")
    private List<String> chunkServers;

    // Number of chunkServer groups
    private static final int numOfGroups = 2;

    // Temporary directory to store files
    private static final String TEMP_DIR = "temp/";

    // endpoint to add a chunk server
    @PostMapping("/addChunkServer")
    public ResponseEntity<String> addChunkServer(@RequestParam("chunkServerUrl") String chunkServerUrl) {
        if (!chunkServers.contains(chunkServerUrl))
        {
            System.out.println("Adding chunk server: " + chunkServerUrl);
            chunkServers.add(chunkServerUrl);
        }
        return ResponseEntity.ok("Chunk server added successfully.");
    }

    // Endpoint to upload and distribute a file
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("chunkSize") int chunkSize) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        try {
            // Generate a unique file name to avoid overwriting existing files
            Path filePath = Paths.get(TEMP_DIR, file.getOriginalFilename());

            // Create the file and write the content
            new File(TEMP_DIR).mkdirs();
            Files.copy(file.getInputStream(), filePath);

            File uploadedFile = filePath.toFile();

            // Split file into chunks and distribute them
            distributeChunks(uploadedFile, chunkSize);

            // Delete temporary file
            uploadedFile.delete();

            return ResponseEntity.ok("File uploaded and distributed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during file upload: " + e.getMessage());
        }
    }

    private void distributeChunks(File inputFile, int chunkSize) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        byte[] buffer = new byte[chunkSize];
        int bytesRead;
        String fileName = inputFile.getName();
    
        List<List<String>> serverGroups = new ArrayList<>();
        for (int i = 0; i < numOfGroups; i++) {
            serverGroups.add(new ArrayList<>());
        }
    
        for (int i = 0; i < chunkServers.size(); i++) {
            serverGroups.get(i % numOfGroups).add(chunkServers.get(i));
        }

        // print server groups
        for (int i = 0; i < numOfGroups; i++) {
            System.out.println("Group " + i + ": " + serverGroups.get(i));
        }
    
        // Map to store chunks to servers mapping
        Map<Integer, List<String>> chunkToServersMap = new LinkedHashMap<>();
    
        // Read the file and create chunks
        int chunkNumber = 0;
        while ((bytesRead = fis.read(buffer)) > 0) {
            // Create a temporary file for each chunk
            File chunkFile = new File(TEMP_DIR + fileName + ".chunk" + chunkNumber);
            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                fos.write(buffer, 0, bytesRead);
            }
    
            // Assign chunk to a group of servers
            List<String> assignedServers = serverGroups.get(chunkNumber % numOfGroups);
            chunkToServersMap.put(chunkNumber, new ArrayList<>(assignedServers));
    
            // Delete the temporary chunk file
            chunkFile.delete();
            chunkNumber++;
        }
    
        fis.close();
        fileToChunkNumMap.put(fileName, chunkToServersMap);
    }

    // Endpoint to get all file-to-chunk-server mapping
    @GetMapping("/getFileMapping")
    public ResponseEntity<Map<String, Map<Integer, List<String>>>> getFileMapping() {
        // check if fileToChunkNumMap is empty
        if (fileToChunkNumMap.isEmpty()) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(fileToChunkNumMap);
    }

    // Endpoint to get chunk servers for a file
    @GetMapping("/getChunkServers")
    public ResponseEntity<Map<Integer, List<String>>> getChunkServers(@RequestParam("fileName") String fileName) {
        // check if file exists in fileToChunkNumMap
        if (!fileToChunkNumMap.containsKey(fileName)) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(fileToChunkNumMap.get(fileName));
    }
}
