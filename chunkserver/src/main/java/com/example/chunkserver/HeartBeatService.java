package com.example.chunkserver;

import com.example.chunkserver.entity.ChunkServerHeartbeat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class HeartBeatService {
    @Value("${chunkmaster.url}")
    private String chunkMasterUrl;

    public void sendHeartBeatToMaster(String url, Map<String, List<String>> chunkStorageMap) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = chunkMasterUrl + "/chunkServerHeartBeat";

        ChunkServerHeartbeat heartbeat = new ChunkServerHeartbeat(url, chunkStorageMap);
        try {
            restTemplate.postForEntity(requestUrl, heartbeat, String.class);
            System.out.println("Sent HeartBeat to master");
        } catch (Exception e) {
            System.err.println("Error sending heartbeat: " + e.getMessage());
        }
    }
}
