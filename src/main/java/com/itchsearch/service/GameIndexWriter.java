package com.itchsearch.service;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds and writes Lucene index for game data.
 * Uses StandardAnalyzer with a custom stopword file (resources/stopword).
 */
public class GameIndexWriter {

    private Directory directory;
    private IndexWriter ixwriter;
    private FieldType contentType;
    private FieldType titleType;

    public GameIndexWriter(String indexDir) throws IOException {
        directory = FSDirectory.open(Paths.get(indexDir));

        // load stopwords manually
        CharArraySet stopWords = loadStopwords("stopword");

        StandardAnalyzer analyzer = new StandardAnalyzer(stopWords);
        IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        indexConfig.setMaxBufferedDocs(10000);
        ixwriter = new IndexWriter(directory, indexConfig);

        contentType = new FieldType();
        contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        contentType.setStored(true);
        contentType.setStoreTermVectors(true);

        titleType = new FieldType();
        titleType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
        titleType.setStored(true);
        titleType.setStoreTermVectors(false);
    }

    private CharArraySet loadStopwords(String filename) throws IOException {
        InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
        if (input == null) {
            System.out.println("No stopword file found, using default StandardAnalyzer stopwords.");
            return CharArraySet.EMPTY_SET;
        }

        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    words.add(line);
                }
            }
        }

        System.out.println("Loaded custom stopword file ('" + filename + "') with " + words.size() + " words.");
        return new CharArraySet(words, true);
    }

    public void indexGame(String gameId, String title, String author, String price, String thumbnail, String description, String url) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField("GAME_ID", gameId));
        doc.add(new Field("TITLE", title, titleType));
        doc.add(new Field("AUTHOR", author != null ? author : "Unknown", titleType));
        doc.add(new Field("PRICE", price != null ? price : "N/A", titleType));
        doc.add(new Field("THUMBNAIL", thumbnail != null ? thumbnail : "", titleType));
        doc.add(new Field("URL", url != null ? url : "", titleType));
        doc.add(new Field("CONTENT", description != null ? description : "", contentType));
        ixwriter.addDocument(doc);
    }


    public void close() throws IOException {
        ixwriter.close();
        directory.close();
    }
}