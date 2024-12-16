package com.example.client.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChunkToChunkMaster {
    String id;
    String fileName;
    int order;
}

