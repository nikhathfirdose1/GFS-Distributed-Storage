package com.example.chunkserver;

import com.example.chunkserver.entity.ChunkMetadata;
import com.example.chunkserver.entity.ChunkServer;
import com.example.chunkserver.entity.ChunkServerHeartbeat;
import com.example.chunkserver.entity.ServerStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Service
public class HeartBeatService {
    @Value("${chunkmaster.url}")
    private String chunkMasterUrl;

    public void sendHeartBeatToMaster(String chunkServerAddress, Set<ChunkMetadata> storedChunkMetadataSet) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = chunkMasterUrl + "/chunkStatus";



        ChunkServer chunkServer = new ChunkServer(chunkServerAddress, storedChunkMetadataSet);
        ChunkServerHeartbeat heartbeat = new ChunkServerHeartbeat(chunkServer, ServerStatus.ONLINE);
        try {
            restTemplate.postForEntity(requestUrl, heartbeat, String.class);
            System.out.println("Sent HeartBeat to master");
        } catch (Exception e) {
            System.err.println("Error sending heartbeat: " + e.getMessage());
        }
    }
}
