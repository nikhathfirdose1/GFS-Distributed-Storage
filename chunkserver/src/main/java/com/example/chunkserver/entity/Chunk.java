package com.example.chunkserver.entity;


public class Chunk {
    String id;
    String content;

    public Chunk(String chunkId, String data) {
        this.id = chunkId;
        this.content = data;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
