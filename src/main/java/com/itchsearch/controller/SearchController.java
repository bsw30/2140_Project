package com.itchsearch.controller;

import com.itchsearch.model.Game;
import com.itchsearch.service.GameSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for handling search requests.
 *
 * Example:
 * GET /api/search?query=visual%20novel
 * Returns: JSON array of games (title, author, price, url, fullDescription)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private GameSearchService gameSearchService;

    @GetMapping(
            value = "/search",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<Game> search(@RequestParam("query") String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of(); // return empty list if no query
        }
        return gameSearchService.searchGames(query.trim(), 50); // limit 50 results
    }
}
