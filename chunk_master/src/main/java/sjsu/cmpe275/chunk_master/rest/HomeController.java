package sjsu.cmpe275.chunk_master.rest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "Welcome to Chunk Master!";
    }
}
