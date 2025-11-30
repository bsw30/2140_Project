package com.itchsearch;

import com.itchsearch.model.GameDocument;
import com.itchsearch.util.TextPreprocessor;
import com.itchsearch.util.TrectextCorpusReader;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Builds Lucene index from TRECTEXT dataset
public class IndexBuilderMain {
    public static void main(String[] args) {
        try {
            //Set paths for input file and index directory
            Path basePath = Paths.get(System.getProperty("user.dir"));
            Path inputPath = basePath.resolve("src/main/resources/data/merged_itchio_dataset.trectext");
            Path indexPath = basePath.resolve("data/index");

            System.out.println("Input file:  " + inputPath);
            System.out.println("Index dir:   " + indexPath);

            //Delete old index folder if exists
            File indexDir = indexPath.toFile();
            if (indexDir.exists()) {
                deleteDirectory(indexDir);
            }
            Files.createDirectories(indexPath);

            //Initialize text preprocessor with Porter Stemmer
            TextPreprocessor preprocessor = new TextPreprocessor();

            //Initialize Lucene index writer with WhitespaceAnalyzer
            IndexWriterConfig config = new IndexWriterConfig(new WhitespaceAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            try (FSDirectory dir = FSDirectory.open(indexPath);
                 IndexWriter writer = new IndexWriter(dir, config);
                 TrectextCorpusReader reader = new TrectextCorpusReader(inputPath.toString())) {

                //Create custom FieldType for CONTENT field with term vectors
                FieldType contentType = new FieldType();
                contentType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
                contentType.setStored(true);
                contentType.setStoreTermVectors(true);
                contentType.setTokenized(true);

                //Create custom FieldType for CONTENT_ORIGINAL with no length limit
                FieldType originalContentType = new FieldType();
                originalContentType.setStored(true);
                originalContentType.setTokenized(false);
                originalContentType.setIndexOptions(IndexOptions.NONE);

                int count = 0;
                GameDocument doc;

                while ((doc = reader.nextDocument()) != null) {
                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("DOCNO", doc.getDocno(), Field.Store.YES));
                    luceneDoc.add(new TextField("TITLE", doc.getTitle(), Field.Store.YES));
                    luceneDoc.add(new StringField("AUTHOR", doc.getAuthor(), Field.Store.YES));
                    luceneDoc.add(new StringField("PRICE", doc.getPrice(), Field.Store.YES));
                    luceneDoc.add(new StringField("URL", doc.getUrl(), Field.Store.YES));

                    //Store original content for display purposes without length limit
                    luceneDoc.add(new Field("CONTENT_ORIGINAL", doc.getText(), originalContentType));

                    //Preprocess content with Porter Stemmer for searching
                    String preprocessedContent = preprocessor.preprocess(doc.getText());
                    luceneDoc.add(new Field("CONTENT", preprocessedContent, contentType));

                    writer.addDocument(luceneDoc);
                    count++;
                }

                System.out.println("Indexed " + count + " documents successfully to: " + indexPath);
            }

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Recursively delete directory and all contents
    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    Files.deleteIfExists(file.toPath());
                }
            }
        }
        Files.deleteIfExists(directory.toPath());
    }
}