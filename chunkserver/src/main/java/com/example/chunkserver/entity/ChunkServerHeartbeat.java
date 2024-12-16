package com.example.chunkserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class ChunkServerHeartbeat {
    ChunkServer chunkServer;
    ServerStatus status;

}
