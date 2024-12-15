package com.example.chunkmaster;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data model for chunk server
 */
@Getter
public class ChunkServer {
    private final String networkAddress;
    private final ConcurrentHashMap<String, ChunkMetadata> chunksById;

    public ChunkServer(String networkAddress, ConcurrentHashMap<String, ChunkMetadata> chunks) {
        this.networkAddress = networkAddress;
        this.chunksById = chunks;
    }

    public int getFileCount() {
        return chunksById.size();
    }

    public void addFile(ChunkMetadata chunk){
        chunksById.put(chunk.id(), chunk);
    }

    // Static method to sort ChunkServers by file list size
    public static ArrayList<String> sortByFileCountAscending(ConcurrentHashMap<String, ChunkServer> chunkServersByNetworkAddress) {
        ArrayList<String> sortedServerAddresses = new ArrayList<>();

        ArrayList<ChunkServer> servers = new ArrayList<>(chunkServersByNetworkAddress.values());
        List<ChunkServer> sortedServers = servers.stream()
                .sorted(Comparator.comparingInt(ChunkServer::getFileCount))
                .collect(Collectors.toList());

        for(ChunkServer _server: sortedServers){
            sortedServerAddresses.add(_server.networkAddress);
        }

        return sortedServerAddresses;
    }
}
