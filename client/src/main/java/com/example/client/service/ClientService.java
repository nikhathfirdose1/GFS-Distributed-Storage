package com.example.client.service;

import com.example.client.entity.Chunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClientService {

    public List<Chunk> split(byte[] fileData){
        //logic to split a file into chunks
        return new ArrayList<>();
    }
}
