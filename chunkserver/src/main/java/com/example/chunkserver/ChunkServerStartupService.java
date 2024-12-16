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

        int maxRetries = 3;
        int retryCount = 0;
        boolean success = false;

        /*while (retryCount < maxRetries && !success) {
            try {
                System.out.println("Attempting to register Chunk Server. Attempt: " + (retryCount + 1));
                ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, null, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Successfully registered with Chunk Master");
                    success = true;
                } else {
                    System.err.println("Failed to register with Chunk Master. Status Code: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Error occurred during registration attempt " + (retryCount + 1) + ": " + e.getMessage());
            }

            if (!success) {
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        System.out.println("Retrying in 3 seconds...");
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        if (!success) {
            throw new RuntimeException("Failed to register with Chunk Master after " + maxRetries + " attempts");
        }*/
    }
}
