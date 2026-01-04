package com.flight.restweb;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flights")
public class SearchController {

    @PostMapping("/search")
    public String search() {
        return "Hello World";
    }
}
