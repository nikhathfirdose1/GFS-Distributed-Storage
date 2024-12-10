package com.example.chunkmaster;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@SpringBootApplication
public class ChunkMasterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChunkMasterApplication.class, args);
    }
}
