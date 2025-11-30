package com.itchsearch.util;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Component
public class TextPreprocessor {

    private Set<String> stopwords;

    public TextPreprocessor() {
        this.stopwords = new HashSet<>();
        loadStopwords();
    }

    //Load stopwords from resources file
    private void loadStopwords() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("stopwords.txt");
            if (is == null) {
                System.err.println("Warning: stopwords.txt not found");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) {
                    stopwords.add(line);
                }
            }
            br.close();
            System.out.println("Loaded " + stopwords.size() + " stopwords");
        } catch (IOException e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
        }
    }

    //Preprocess text with tokenization, lowercase, stopword removal and stemming
    public String preprocess(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] tokens = text.split("[^a-zA-Z0-9]+");
        StringBuilder processed = new StringBuilder();
        Stemmer stemmer = new Stemmer();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            token = token.toLowerCase();

            if (stopwords.contains(token)) continue;

            char[] chars = token.toCharArray();
            stemmer.add(chars, chars.length);
            stemmer.stem();
            String stemmedToken = stemmer.toString();

            if (!stemmedToken.isEmpty()) {
                if (processed.length() > 0) {
                    processed.append(" ");
                }
                processed.append(stemmedToken);
            }
        }

        return processed.toString();
    }
}