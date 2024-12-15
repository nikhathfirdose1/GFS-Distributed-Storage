package com.example.chunkmaster;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chunkMaster")
public class HeartbeatController {
    @PostMapping("/chunkServerHeartBeat")
    public void handleChunkServerHeartBeat(@RequestBody ChunkServerHeartbeat chunkServerHeartbeat){
        System.out.println("Received ChunkServerHeartbeat from: " + chunkServerHeartbeat.getChunkServerUrl());
        //TO-DO: handle chunkServerHeartbeat
    }
}
