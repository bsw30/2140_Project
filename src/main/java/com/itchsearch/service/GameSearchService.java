package com.itchsearch.service;

import com.itchsearch.util.Stemmer;
import com.itchsearch.model.Game;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.nio.charset.StandardCharsets;


//Search service using bag of words model with Dirichlet smoothing
@Service
public class GameSearchService {

    private Map<Integer, Integer> docLengthCache = new HashMap<>();
    private Map<String, Long> collectionFreqCache = new HashMap<>();

    private static final String INDEX_DIR = "data/index";
    private static final double MU = 2000.0;

    public List<Game> searchGames(String queryStr, int maxResults) {
        List<Game> results = new ArrayList<>();
        Path indexPath = Paths.get(System.getProperty("user.dir")).resolve(INDEX_DIR);

        //Clear cache to ensure fresh data from index
        docLengthCache.clear();
        collectionFreqCache.clear();

        try (FSDirectory dir = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(dir)) {

            //Preprocess query using Porter Stemmer
            String[] queryTerms = preprocessQuery(queryStr);
            if (queryTerms.length == 0) {
                return results;
            }

            //Use a set of query stems for highlighting
            Set<String> queryStemSet = new HashSet<>(Arrays.asList(queryTerms));

            //Calculate collection statistics from actual index
            long collectionLength = calculateCollectionLength(reader);

            //Score all documents
            Map<Integer, Double> docScores = new HashMap<>();
            IndexSearcher searcher = new IndexSearcher(reader);

            for (int docId = 0; docId < reader.maxDoc(); docId++) {
                double score = scoreDocument(reader, docId, queryTerms, collectionLength);
                if (score > Double.NEGATIVE_INFINITY) {
                    docScores.put(docId, score);
                }
            }

            //Sort by score and get top N
            List<Map.Entry<Integer, Double>> sortedDocs = new ArrayList<>(docScores.entrySet());
            sortedDocs.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

            int limit = Math.min(maxResults, sortedDocs.size());
            for (int i = 0; i < limit; i++) {
                int docId = sortedDocs.get(i).getKey();
                Document doc = searcher.doc(docId);

                Game game = new Game();
                game.setTitle(doc.get("TITLE"));
                game.setAuthor(doc.get("AUTHOR"));
                game.setPrice(doc.get("PRICE"));
                game.setUrl(doc.get("URL"));

                String originalDescription = doc.get("CONTENT_ORIGINAL");
                game.setFullDescription(originalDescription);

                //Generate highlighted description using backend stems
                String highlighted = highlightDescriptionWithStems(originalDescription, queryStemSet);
                game.setHighlightedDescription(highlighted);

                results.add(game);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }


    //Preprocess query using Porter Stemmer same as assignment
    private String[] preprocessQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new String[0];
        }

        //Tokenize by splitting on non-alphanumeric characters
        String[] tokens = query.toLowerCase().split("[^a-zA-Z0-9]+");
        List<String> processedTokens = new ArrayList<>();

        //Load stopwords
        Set<String> stopwords = loadStopwords();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            //Skip stopwords
            if (stopwords.contains(token)) continue;

            //Apply Porter Stemmer
            Stemmer stemmer = new Stemmer();
            char[] chars = token.toCharArray();
            stemmer.add(chars, chars.length);
            stemmer.stem();
            String stemmedToken = stemmer.toString();

            if (!stemmedToken.isEmpty()) {
                processedTokens.add(stemmedToken);
            }
        }

        return processedTokens.toArray(new String[0]);
    }

    //Load stopwords from file
    private Set<String> loadStopwords() {
        Set<String> stopwords = new HashSet<>();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("stopwords.txt");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    stopwords.add(word);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
        }
        return stopwords;
    }

    //Score document using Query Likelihood Model with Dirichlet Prior Smoothing
    private double scoreDocument(DirectoryReader reader, int docId, String[] queryTerms, long collectionLength)
            throws IOException {

        double score = 0.0;
        int docLength = getDocLength(reader, docId);

        if (docLength == 0) {
            return Double.NEGATIVE_INFINITY;
        }

        //Check if document contains at least one query term
        boolean containsAnyTerm = false;

        for (String term : queryTerms) {
            if (term.isEmpty()) continue;

            int termFreqInDoc = getTermFreqInDoc(reader, docId, term);

            if (termFreqInDoc > 0) {
                containsAnyTerm = true;
            }

            long collectionFreq = getCollectionFreq(reader, term);

            double probTermInCollection = (double) collectionFreq / collectionLength;
            double probTermInDoc = (termFreqInDoc + MU * probTermInCollection) / (docLength + MU);

            //Use log probability to avoid underflow
            if (probTermInDoc > 0) {
                score += Math.log(probTermInDoc);
            }
        }

        //Only return score if document contains at least one query term
        if (!containsAnyTerm) {
            return Double.NEGATIVE_INFINITY;
        }

        return score;
    }

    //Get document length from CONTENT field with caching
    private int getDocLength(DirectoryReader reader, int docId) throws IOException {
        if (docLengthCache.containsKey(docId)) {
            return docLengthCache.get(docId);
        }

        Terms terms = reader.getTermVector(docId, "CONTENT");
        if (terms == null) {
            return 0;
        }

        int length = 0;
        TermsEnum termsEnum = terms.iterator();
        while (termsEnum.next() != null) {
            length += termsEnum.totalTermFreq();
        }

        docLengthCache.put(docId, length);
        return length;
    }

    //Get term frequency in document CONTENT field
    private int getTermFreqInDoc(DirectoryReader reader, int docId, String term) throws IOException {
        Terms terms = reader.getTermVector(docId, "CONTENT");
        if (terms == null) {
            return 0;
        }

        TermsEnum termsEnum = terms.iterator();
        if (termsEnum.seekExact(new BytesRef(term))) {
            PostingsEnum postings = termsEnum.postings(null, PostingsEnum.FREQS);
            if (postings != null && postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
                return postings.freq();
            }
        }
        return 0;
    }

    //Get collection frequency with caching
    private long getCollectionFreq(DirectoryReader reader, String term) throws IOException {
        if (collectionFreqCache.containsKey(term)) {
            return collectionFreqCache.get(term);
        }

        long totalFreq = 0;
        for (int i = 0; i < reader.leaves().size(); i++) {
            Terms terms = reader.leaves().get(i).reader().terms("CONTENT");
            if (terms != null) {
                TermsEnum termsEnum = terms.iterator();
                if (termsEnum.seekExact(new BytesRef(term))) {
                    totalFreq += termsEnum.totalTermFreq();
                }
            }
        }

        collectionFreqCache.put(term, totalFreq);
        return totalFreq;
    }

    //Calculate total collection length
    private long calculateCollectionLength(DirectoryReader reader) throws IOException {
        long total = 0;
        for (int i = 0; i < reader.maxDoc(); i++) {
            total += getDocLength(reader, i);
        }
        return total;
    }

    //Return stemmed query for frontend highlighting
    public String getStemmedQuery(String query) {
        String[] stemmedTerms = preprocessQuery(query);
        return String.join(" ", stemmedTerms);
    }

    //Highlight words in the original text whose stems match any query stem
    private String highlightDescriptionWithStems(String text, Set<String> queryStems) {
        if (text == null || text.isEmpty() || queryStems == null || queryStems.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isLetterOrDigit(c)) {
                //Build the current token
                currentToken.append(c);
            } else {
                //Flush the current token if any
                if (currentToken.length() > 0) {
                    String token = currentToken.toString();
                    String stem = stemToken(token);

                    if (queryStems.contains(stem)) {
                        result.append("<span class=\"highlight\">").append(token).append("</span>");
                    } else {
                        result.append(token);
                    }
                    currentToken.setLength(0);
                }

                //Append the non-word character as is
                result.append(c);
            }
        }

        //Flush last token if any
        if (currentToken.length() > 0) {
            String token = currentToken.toString();
            String stem = stemToken(token);

            if (queryStems.contains(stem)) {
                result.append("<span class=\"highlight\">").append(token).append("</span>");
            } else {
                result.append(token);
            }
        }

        return result.toString();
    }

    //Stem a single token using the same Stemmer as in indexing and scoring
    private String stemToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        String lower = token.toLowerCase(Locale.ROOT);
        Stemmer stemmer = new Stemmer();
        char[] chars = lower.toCharArray();
        stemmer.add(chars, chars.length);
        stemmer.stem();
        return stemmer.toString();
    }

}