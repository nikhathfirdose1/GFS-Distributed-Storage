package com.example.chunkmaster;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Service
public class HeartBeatService {

    private final RestTemplate restTemplate = new RestTemplate();
    private ChunkMasterState state = new ChunkMasterState();

    public void sendHeartBeatToChunkServer(Queue<String> chunkServerQueue) {
        Iterator<String> iterator = chunkServerQueue.iterator();
        while (iterator.hasNext()) {
            String chunkServerAddress = iterator.next();
            String url = "http://" + chunkServerAddress + "/heartbeat";
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Heartbeat sent successfully to " + chunkServerAddress);
                } else {
                    System.out.println("Failed to send heartbeat to " + chunkServerAddress);
                    iterator.remove();
                    handleHeartbeatError(chunkServerAddress);
                }
            } catch (Exception e) {
                System.out.println("Error sending heartbeat to " + chunkServerAddress + ": " + e.getMessage());
                iterator.remove();
                state.chunkServersByNetworkAddress().remove(chunkServerAddress);
            }
        }
    }

    private void handleHeartbeatError(String chunkServerAddress) {
        state.chunkServersByNetworkAddress().remove(chunkServerAddress);

        for (Map<String, List<String>> chunkMap : state.fileToChunkNumMap().values()) {
            for (List<String> serverList : chunkMap.values()) {
                serverList.remove(chunkServerAddress);
            }
        }
    }
}
