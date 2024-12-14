package com.example.chunkserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import com.example.chunkserver.entity.Chunk;
import org.springframework.stereotype.Service;

@Service
public class ChunkServerService {
    public void storeChunk(String filename, Chunk chunk) throws RuntimeException {
        String basePath = System.getProperty("user.dir") + File.separator + "files";
        File directory = new File(basePath, filename);

        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs();
            if (!dirCreated) {
                System.err.println("Failed to create directory: " + directory.getAbsolutePath());
                return;
            }
        }

        File chunkFile = new File(directory, chunk.getId() + ".txt");
        try (FileWriter writer = new FileWriter(chunkFile)) {
            writer.write(chunk.getContent());
            System.out.println("Chunk stored successfully: " + chunkFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error writing chunk to file: " + chunkFile.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public Map<String, List<Chunk>> retrieveChunks() {
        Map<String, List<Chunk>> storage = new HashMap<>();
        File baseDirectory = new File(System.getProperty("user.dir")+ File.separator + "files");

        if (!baseDirectory.exists()){
            if (baseDirectory.mkdirs()) {
                System.out.println("Directory created: " + baseDirectory.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + baseDirectory.getAbsolutePath());
            }
        }

        for (File dir : Objects.requireNonNull(baseDirectory.listFiles(File::isDirectory))) {
            List<Chunk> chunks = new ArrayList<>();

            for (File file : Objects.requireNonNull(dir.listFiles((d, name) -> name.endsWith(".txt")))) {
                try {
                    String content = Files.readString(file.toPath());
                    String chunkId = file.getName().replace(".txt", "");
                    chunks.add(new Chunk(chunkId, content));
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            storage.put(dir.getName(), chunks);
        }
        return storage;
    }

}
