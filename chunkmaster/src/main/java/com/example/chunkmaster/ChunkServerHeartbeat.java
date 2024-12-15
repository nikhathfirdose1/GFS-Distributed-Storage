package com.example.chunkmaster;

import java.util.List;
import java.util.Map;

public class ChunkServerHeartbeat {
    String chunkServerUrl;
    Map<String, List<String>> chunkStorage; //map of filename, list of chunkIds
    public ChunkServerHeartbeat(String chunkServerUrl, Map<String, List<String>> chunkStorage){
        this.chunkServerUrl = chunkServerUrl;
        this.chunkStorage = chunkStorage;
    }

    public String getChunkServerUrl(){ return chunkServerUrl; }
    public Map<String, List<String>> getChunkStorage(){return chunkStorage; }
}
