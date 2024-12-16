package com.example.client.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChunkToChunkServer {
    String id;
    String content;
    String filename;
    int order;
}
