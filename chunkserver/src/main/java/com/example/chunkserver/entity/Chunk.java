package com.example.chunkserver.entity;

import lombok.Getter;

@Getter
public class Chunk {
    String id;
    String content;
    String filename;
    int order;

    public Chunk(String chunkId, String data, int order) {
        this.id = chunkId;
        this.content = data;
        this.order = order;
    }
}
