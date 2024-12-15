package com.example.chunkmaster;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class encapsulating Master State with chunk server data and file mapping
 */
public class ChunkMasterState {

    private final ConcurrentHashMap<String, Map<String, List<String>>> fileToChunkNumMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChunkServer> chunkServersByNetworkAddress = new ConcurrentHashMap<>();

    /**
     * Function to handle fault tolerance on these scenarios:
     * - Chunk server notifies of an error
     * - Master server cannot contact chunk server
     * NOTE: Logic should copy chunks lost to another available server to keep replication
     */
    public void handleServerError(){
        // TODO: Start fault tolerance code here
    }

    /**
     * Function to update master state based on hearbeat information received from chunk servers
     * @param heartbeat Information from chunk server
     */
    public void updateChunkServerState(Heartbeat heartbeat){
        switch (heartbeat.getStatus()){
            case ONLINE -> {
                // Update network address by network
                chunkServersByNetworkAddress.put(heartbeat.getChunkServer().getNetworkAddress(), heartbeat.getChunkServer());

                // Update fileToChunkNumMap
                Collection<ChunkMetadata> chunksInServer = heartbeat.getChunkServer().getChunksById().values();
                String serverAddress = heartbeat.getChunkServer().getNetworkAddress();

                for(ChunkMetadata chunkMetadata: chunksInServer){
                    Map<String, List<String>> networkAddressesForChunk = fileToChunkNumMap.get(chunkMetadata.fileName());
                    if(networkAddressesForChunk == null){
                        networkAddressesForChunk = new HashMap<>();
                    }
                    List<String> networkAddresses = networkAddressesForChunk.get(chunkMetadata.id());
                    if(networkAddresses == null){
                        networkAddresses = new ArrayList<>();
                    }
                    if(!networkAddresses.contains(serverAddress)){
                        networkAddresses.add(serverAddress);
                    }
                    networkAddressesForChunk.put(chunkMetadata.id(), networkAddresses);
                    fileToChunkNumMap.put(chunkMetadata.fileName(), networkAddressesForChunk);
                }
            }
            case ERROR -> {
               handleServerError();
            }
        }
    }

    /**
     * Function to read file content mapping
     * @param fileName name of file
     * @return Chunks mapped to server addresses
     */
    public Pair<Optional<FileMappingResponse>, Optional<Error>> readFile(String fileName){
        Map<String, List<String>> file = fileToChunkNumMap.get(fileName);
        if(file != null){
            return Pair.of(Optional.of(new FileMappingResponse(file)), Optional.empty());
        }else {
           return Pair.of(Optional.empty(), Optional.of(Error.FILE_NOT_PRESENT)); // File is not present, handle error case scenario
        }
    }

    /**
     *
     * Function to obtain mapping where file should be stored
     * NOTE: this function will not store the file in memory. The chunk server should notify
     * master once the write process has completed
     *
     * @param fileName name of the file
     * @param chunks pieces of the file as received from client
     * @param numCopies replication factor
     * @return Suggested mapping where file contents should be stored
     */
    public Pair<Optional<FileMappingResponse>, Optional<Error>> writeFile(String fileName, ArrayList<ChunkMetadata> chunks, int numCopies){
        if(numCopies > chunkServersByNetworkAddress.size()){
            return Pair.of(Optional.empty(), Optional.of(Error.NOT_ENOUGH_CHUNK_SERVERS)); // Handle error where cluster does not have enough servers for replication
        }

        if(fileToChunkNumMap.get(fileName) != null){
            return Pair.of(Optional.empty(), Optional.of(Error.FILE_ALREADY_EXISTS)); // Handle scenario where file already exists
        }

        HashMap<String, List<String>> chunkToServerAddress = new HashMap<>();
        ConcurrentHashMap<String, ChunkServer> chunkServersByNetworkAddressCopy = chunkServersByNetworkAddress;
        for(ChunkMetadata _chunk: chunks){
            // Sort by number of file chunks stored
            ArrayList<String> serversByCapacity = ChunkServer.sortByFileCountAscending(chunkServersByNetworkAddressCopy);
            ArrayList<String> sublistNetworkAddress = new ArrayList<>(serversByCapacity.subList(0, numCopies));
            chunkToServerAddress.put(_chunk.id(), sublistNetworkAddress);

            // Update optimistic state
            for(String networkAddress: sublistNetworkAddress){
                ChunkServer server = chunkServersByNetworkAddressCopy.get(networkAddress);
                if(server == null){
                    continue;
                }
                server.addFile(_chunk);
                chunkServersByNetworkAddressCopy.put(server.getNetworkAddress(), server);
            }
        }

        return Pair.of(Optional.of(new FileMappingResponse(chunkToServerAddress)), Optional.empty());
    }

    /**
     * Getter for all chunk server data by network address
     * @return All chunk server data by network address
     */
    public ConcurrentHashMap<String, ChunkServer> chunkServersByNetworkAddress() {
        return chunkServersByNetworkAddress;
    }

    /**
     * Getter for all files with its content mapping to each server address
     * @return All files with its content mapping to each server address
     */
    public Map<String, Map<String, List<String>>> allMapping(){
        return fileToChunkNumMap;
    }
}
