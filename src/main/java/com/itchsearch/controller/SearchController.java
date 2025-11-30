package com.itchsearch.controller;

import com.itchsearch.model.Game;
import com.itchsearch.service.GameSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//REST controller for search endpoint
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private GameSearchService gameSearchService;

    //GET request handler for search with stemmed query returned
    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String query) {
        List<Game> games = gameSearchService.searchGames(query, 50);
        String stemmedQuery = gameSearchService.getStemmedQuery(query);

        Map<String, Object> response = new HashMap<>();
        response.put("results", games);
        response.put("stemmedQuery", stemmedQuery);

        return response;
    }
}