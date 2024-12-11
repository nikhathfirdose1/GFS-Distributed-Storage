package com.example.chunkmaster;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "Welcome to Chunk Master!";
    }
}
