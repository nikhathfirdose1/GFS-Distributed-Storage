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
    private final Set<ChunkMetadata> chunks;

    public ChunkServer(String networkAddress, Set<ChunkMetadata> chunks) {
        this.networkAddress = networkAddress;
        this.chunks = chunks;
    }

    public int getFileCount() {
        return chunks.size();
    }

    public void addFile(ChunkMetadata chunk){
        chunks.add(chunk);
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
