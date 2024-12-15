package com.example.chunkserver.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
@Getter @AllArgsConstructor
public class ChunkServerHeartbeat {
    String chunkServerUrl;
    Map<String, List<String>> chunkStorage; //map of filename, list of chunkIds

}
