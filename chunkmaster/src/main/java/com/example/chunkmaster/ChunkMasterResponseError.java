package com.example.chunkmaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Optional;

@Data
public class ChunkMasterResponseError implements Response<FileMappingResponse, String>{

    @JsonIgnore
    private final Pair<Optional<FileMappingResponse>, Optional<Error>> pair;

    public ChunkMasterResponseError(Pair<Optional<FileMappingResponse>, Optional<Error>> pair) {
        this.pair = pair;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Nullable
    @Override
    public FileMappingResponse data() {
        return null;
    }

    @JsonProperty("error")
    @Nullable
    @Override
    public String error() {
        if(pair.second().isPresent()){
            return pair.second().get().name();
        } else {
            return null;
        }
    }
}
