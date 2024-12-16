package com.example.client;

/**
 * Object used by client to distribute data
 */
public class Chunk {
    private String uuid;
    private String data;

    public Chunk(String uuid, String data) {
        this.uuid = uuid;
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public String getData() {
        return data;
    }
}
