package com.example.client.entity.response;

import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class MapFileResponse {
    private Map<String, List<String>> chunkAddressMap;
}
