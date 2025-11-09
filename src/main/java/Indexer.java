package main.java;
// read CSV and build Lucene Index //
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.opencsv.CSVReader;

public class Indexer {

    private static final String INDEX_DIRECTORY = "index";
    // DATA_FILE_PATH will now be set by the constructor
    private final String dataFilePath; 
    private IndexWriter writer;

    // Modified constructor to accept the data file path
    public Indexer(String dataFilePath) throws IOException {
        this.dataFilePath = dataFilePath;
        
        // 1. Directory: Location where the index files are stored
        Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // 2. Analyzer: Defines how text is processed before indexing
        Analyzer analyzer = new StandardAnalyzer();

        // 3. IndexWriterConfig: Configuration for the IndexWriter
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(OpenMode.CREATE); // Always recreate the index for this project
        config.setSimilarity(new BM25Similarity());
        
        // 4. IndexWriter: Core component for adding documents
        this.writer = new IndexWriter(indexDir, config);
        System.out.println("Indexer initialized. Index location: " + INDEX_DIRECTORY);
        System.out.println("Using BM25 similarity for ranking.");
    }

    public void close() throws IOException {
        if (writer != null) {
            long numDocs = writer.getPendingNumDocs();
            writer.close();
            System.out.println("IndexWriter closed. Total documents indexed: " + numDocs);
        }
    }

    private void addDocument(String id, String title, String description, String url) throws IOException {
        Document doc = new Document();

        // Stored fields for retrieval (StringField for exact match/storage)
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new StringField("url", url, Field.Store.YES));

        // Searchable fields (TextField for analysis and full-text search)
        // Store.YES on title is useful for displaying results quickly.
        doc.add(new TextField("title", title, Field.Store.YES));

        // Store.NO on description to save index space, rely on title/id for retrieval.
        doc.add(new TextField("description", description, Field.Store.NO));

        writer.addDocument(doc);
    }

    public void indexData() throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Starting indexing process...");

        try {
            // --- CSV READING LOGIC IMPLEMENTED ---
            
            // Check if the file exists before trying to read it
            if (!Files.exists(Paths.get(dataFilePath))) {
                // Throw an error that includes the path provided
                throw new IOException("Data file not found at: " + dataFilePath);
            }

            try (Reader reader = Files.newBufferedReader(Paths.get(dataFilePath));
                 CSVReader csvReader = new CSVReader(reader)) {
                
                // Read the header row and discard it
                String[] header = csvReader.readNext(); 
                if (header == null) {
                    System.err.println("CSV file is empty or header is missing.");
                    return;
                }
                
                String[] nextRecord;
                int count = 0;
                while ((nextRecord = csvReader.readNext()) != null) {
                    // Ensure the record has enough columns (expecting 4: id, title, description, url)
                    if (nextRecord.length >= 4) { 
                        // nextRecord[0]=id, nextRecord[1]=title, nextRecord[2]=description, nextRecord[3]=url
                        addDocument(nextRecord[0], nextRecord[1], nextRecord[2], nextRecord[3]);
                        count++;
                    } else {
                        System.err.println("Skipping malformed record: " + String.join(",", nextRecord));
                    }
                }
                System.out.println("Successfully read and added " + count + " records from CSV.");
            }
            
            // --- END OF CSV READING LOGIC ---

            writer.commit();

            long endTime = System.currentTimeMillis();
            System.out.println("Indexing finished successfully in " + (endTime - startTime) + " ms.");

        } catch (Exception e) {
            System.err.println("An error occurred during indexing: " + e.getMessage());
            // Rollback changes if an error occurs to keep the index clean
            writer.rollback();
            throw new IOException("Indexing failed.", e);
        }
    }

    public static void main(String[] args) {
        // CHECK: Ensure a file path is provided as the first argument
        if (args.length < 1) {
            System.err.println("Usage: java -jar <your-jar-file> <path/to/data/file.csv>");
            System.err.println("Example: java -jar lucene-demo.jar itchio_games.csv");
            return; 
        }

        String dataFilePath = args[0];
        Indexer indexer = null;
        try {
            // Pass the command line argument to the Indexer constructor
            indexer = new Indexer(dataFilePath);
            indexer.indexData();
        } catch (IOException e) {
            System.err.println("Fatal error during indexer execution.");
            e.printStackTrace();
        } finally {
            try {
                if (indexer != null) {
                    indexer.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing indexer: " + e.getMessage());
            }
        }
    }
}
