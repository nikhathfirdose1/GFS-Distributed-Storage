package com.example.client.entity.request;
import com.example.client.entity.Chunk;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WriteToChunkRequest {
    Chunk chunk;
    List<String> address;
}
