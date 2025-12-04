package com.itchsearch;

import com.itchsearch.model.Game;
import com.itchsearch.service.GameSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Order(1)
public class SearchEvaluationTest implements CommandLineRunner {

    @Autowired
    private GameSearchService searchService;
    
    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("RUNNER EXECUTING");
        System.out.println("Args: " + Arrays.toString(args));
        System.out.println("========================================");
        System.out.flush();
        
        boolean hasEvalFlag = false;
        for (String arg : args) {
            if (arg.equals("--evaluation")) {
                hasEvalFlag = true;
                break;
            }
        }
        
        if (!hasEvalFlag) {
            System.out.println("Web server mode - visit http://localhost:8080");
            return;
        }
        
        System.out.println("\n=== SEARCH ENGINE EVALUATION ===\n");

        String[][] testData = {
            {"Mae Borowski", "night in the woods,mae,psychological,narrative"},
            {"Jan Willem Nijman", "minit,jan willem,time-loop,2d adventure"},
            {"award-winning puzzle", "baba is you,puzzle,award,rules"},
            {"visual novel horror", "doki doki,monika,visual novel,horror"},
            {"surreal puzzle", "oneshot,surreal,meta,puzzle"},
            {"Maddy Thorson", "celeste,maddy thorson,platformer,pico"},
            {"Rourke military", "long gone days,rourke,jrpg,military"},
            {"crocodiles", "hidden folks,crocodile,hidden object,hand-drawn"},
            {"time-loop adventure", "minit,time,loop,adventure"},
            {"exploration 3d", "short hike,exploration,3d,adventure"}
        };
        
        int totalRelevant = 0;
        List<Double> precisions = new ArrayList<>();
        
        for (int i = 0; i < testData.length; i++) {
            String query = testData[i][0];
            String[] keywords = testData[i][1].split(",");
            
            List<Game> results = searchService.searchGames(query, 10);

            int relevant = 0;
            for (Game g : results) {
                String text = (g.getTitle() + " " + g.getFullDescription()).toLowerCase();
                for (String kw : keywords) {
                    if (text.contains(kw.toLowerCase())) {
                        relevant++;
                        break;
                    }
                }
            }
            
            double precision = results.size() > 0 ? (double)relevant / results.size() : 0;
            precisions.add(precision);
            totalRelevant += relevant;
            
            System.out.println("Query " + (i+1) + ": \"" + query + "\"");
            System.out.println("  Retrieved: " + results.size() + ", Relevant: " + relevant);
            System.out.println("  Precision@10: " + String.format("%.3f", precision));
            
            for (int j = 0; j < Math.min(3, results.size()); j++) {
                System.out.println("    " + (j+1) + ". " + results.get(j).getTitle());
            }
            System.out.println();
        }
        
        double avgPrecision = precisions.stream().mapToDouble(d -> d).average().orElse(0);
        
        System.out.println("=== SUMMARY ===");
        System.out.println("Total queries: " + testData.length);
        System.out.println("Mean Precision@10: " + String.format("%.3f", avgPrecision));
        System.out.println("Avg relevant: " + String.format("%.1f", (double)totalRelevant / testData.length));
        
        System.exit(SpringApplication.exit(context, () -> 0));
    }
}

