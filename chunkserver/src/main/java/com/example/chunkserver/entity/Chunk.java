package com.example.chunkserver.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Chunk {
    String id;
    String content;
}
