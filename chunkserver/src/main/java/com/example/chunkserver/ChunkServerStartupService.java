package com.example.chunkserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChunkServerStartupService {

    @Value("${chunkmaster.url}")
    private String chunkMasterUrl;

    @Value("${server.port}")
    private int serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String chunkServerIp = "127.0.0.1"; // Replace with actual IP if needed
        String chunkServerUrl = "http://" + chunkServerIp + ":" + serverPort;

        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = chunkMasterUrl + "/addChunkServer?chunkServerUrl=" + chunkServerUrl;
        System.out.println(requestUrl);
        ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, null, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Successfully registered with Chunk Master");
        } else {
            throw new RuntimeException("Failed to register with Chunk Master");
        }
    }
}
