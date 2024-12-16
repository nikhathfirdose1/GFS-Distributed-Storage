package com.example.chunkserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter @AllArgsConstructor
public class ChunkServer {
    private final String networkAddress;
    private final Set<ChunkMetadata> chunks;
}
