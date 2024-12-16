package com.example.chunkmaster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class ChunkMasterResponseSuccess implements Response<FileMappingResponse, String> {

    @JsonIgnore
    private final Pair<Optional<FileMappingResponse>, Optional<Error>> pair;

    public ChunkMasterResponseSuccess(Pair<Optional<FileMappingResponse>, Optional<Error>> pair) {
        this.pair = pair;
    }

    @JsonProperty("data")
    @Nullable
    public Map<String, List<String>> chunks(){
        if(pair.first().isPresent()){
            return pair.first().get().getChunkToServerAddress();
        }else {
            return null;
        }
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public FileMappingResponse data() {
        if (pair.first().isPresent()) {
            return pair.first().get();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String error() {
        return null;
    }
}
