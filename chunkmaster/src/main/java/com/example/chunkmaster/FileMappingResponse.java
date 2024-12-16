package com.example.chunkmaster;

import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class FileMappingResponse{
    private final Map<String, List<String>> chunkToServerAddress;

    public FileMappingResponse(Map<String, List<String>> chunkToServerAddress) {
        this.chunkToServerAddress = chunkToServerAddress;
    }

    public static Response<FileMappingResponse, String> successFulResponse(Pair<Optional<FileMappingResponse>, Optional<Error>> pair) {
        return new ChunkMasterResponseSuccess(pair);
    }

    public static Response<FileMappingResponse, String> errorResponse(Pair<Optional<FileMappingResponse>, Optional<Error>> pair) {
        return new ChunkMasterResponseError(pair);
    }
}
