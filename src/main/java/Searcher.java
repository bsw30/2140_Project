package main.java;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.queryparser.classic.ParseException; // 💡 Import for clarity

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Searcher {

    private static final String INDEX_DIRECTORY = "index";
    // Define the fields to be searched by default
    private static final String[] SEARCH_FIELDS = {"title", "description"}; 
    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    // 💡 Simplification: Use MultiFieldQueryParser directly since that is what it holds
    private MultiFieldQueryParser parser; 
    private static final int MAX_RESULTS = 10;

    private static class SearchResult {
        public int rank;
        public float score;
        public String id;
        public String title;
        public String url;
        
        public SearchResult(int rank, float score, String id, String title, String url) {
            this.rank = rank;
            this.score = score;
            this.id = id;
            this.title = title;
            this.url = url;
        }
        
        @Override
        public String toString() {
            return String.format("%d. [Score: %.4f] %s (ID: %s)\n   URL: %s", 
                                rank, score, title, id, url);
        }
    }

    public Searcher() throws IOException {
        // 1. IndexReader: Read-only access to the index
        reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
        
        // 2. IndexSearcher: The main component for executing search queries
        searcher = new IndexSearcher(reader);
        
        // 3. Analyzer: Must match the analyzer used for indexing (StandardAnalyzer)
        analyzer = new StandardAnalyzer();
        
        // 4. QueryParser: Use MultiFieldQueryParser for searching across 'title' AND 'description'
        // This is instantiated correctly, but the type in your original code was QueryParser, 
        // which can be confusing since it's actually a MultiFieldQueryParser.
        parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer); 
        
        // 💡 Simplification/Robustness: Set a default operator to improve search results
        // Use 'AND' so that if a user types multiple words, Lucene requires all of them to be present
        // in one of the fields, rather than just one word (which is the default 'OR').
        parser.setDefaultOperator(MultiFieldQueryParser.Operator.AND);
        
        searcher.setSimilarity(new BM25Similarity());
        
        System.out.println("Searcher initialized. Ready to query " + String.join(" and ", SEARCH_FIELDS) + ".");
        System.out.println("Using BM25 similarity for ranking.");
    }

    public List<SearchResult> performSearch(String queryString) throws IOException, ParseException {
        List<SearchResult> results = new ArrayList<>();
        System.out.println("\nSearching for: \"" + queryString + "\"");

        // Convert the user's string input into a Lucene Query object
        // The MultiFieldQueryParser is used here to parse the query string
        Query query = parser.parse(queryString);

        // Execute the search
        TopDocs docs = searcher.search(query, MAX_RESULTS);
        ScoreDoc[] hits = docs.scoreDocs;

        if (hits.length == 0) {
            System.out.println("No results found.");
            return results;
        }

        System.out.println("Found " + docs.totalHits.value + " hits (showing top " + hits.length + "):");

        // Iterate through the results and print the document details
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            String id = doc.get("id");
            String title = doc.get("title");
            String url = doc.get("url");
            float score = hits[i].score;
            
            SearchResult result = new SearchResult(i + 1, score, id, title, url);
            results.add(result);
            System.out.println(result.toString());
        }
        
        return results;
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            System.out.println("IndexReader closed.");
        }
    }


    public static void main(String[] args) {
        Searcher searcher = null;
        try (Scanner scanner = new Scanner(System.in)) {
            // Check 1: Ensure the index exists before initializing the searcher
            if (!Paths.get(INDEX_DIRECTORY).toFile().exists()) {
                System.err.println("Index folder ('" + INDEX_DIRECTORY + "') not found. Please run Indexer first.");
                return;
            }
            
            searcher = new Searcher();
            
            System.out.println("\nEnter a search query (or type 'quit' to exit):");
            while (true) {
                System.out.print("Query> ");
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("quit")) {
                    break;
                }
                if (!line.isEmpty()) {
                    // 💡 Removed generic 'Exception' catch and improved handling
                    try {
                        searcher.performSearch(line);
                    } catch (ParseException pe) {
                        System.err.println("Invalid query format: " + pe.getMessage());
                        // Continue loop to allow user to try again
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Fatal I/O error during searcher execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (searcher != null) {
                    searcher.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing searcher: " + e.getMessage());
            }
        }
    }
}
