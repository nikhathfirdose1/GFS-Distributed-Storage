package com.example.chunkmaster;

import lombok.Getter;

@Getter
public class Heartbeat {
    private final ChunkServer chunkServer;
    private final ServerStatus status;

    public Heartbeat(ChunkServer networkAddress, ServerStatus status) {
        this.chunkServer = networkAddress;
        this.status = status;
    }
}
