package com.example.chunkmaster;

import lombok.Getter;

/**
 * Metadata pertaining to a file chunk
 */
public record ChunkMetadata(String id, String fileName) {
}
