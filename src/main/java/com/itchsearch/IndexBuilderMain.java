package com.itchsearch;

import com.itchsearch.model.GameDocument;
import com.itchsearch.util.TrectextCorpusReader;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Builds Lucene index from the TRECTEXT dataset.
 * Index will be stored under data/index (project root).
 */
public class IndexBuilderMain {
    public static void main(String[] args) {
        try {
            // Base path = project root
            Path basePath = Paths.get(System.getProperty("user.dir"));
            Path inputPath = basePath.resolve("src/main/resources/data/itchio_dataset_full.trectext");
            Path indexPath = basePath.resolve("data/index");

            System.out.println("Input file:  " + inputPath);
            System.out.println("Index dir:   " + indexPath);

            // Delete old index folder manually (no external libs)
            File indexDir = indexPath.toFile();
            if (indexDir.exists()) {
                deleteDirectory(indexDir);
            }
            Files.createDirectories(indexPath);

            // Initialize Lucene index writer
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // always rebuild index

            try (FSDirectory dir = FSDirectory.open(indexPath);
                 IndexWriter writer = new IndexWriter(dir, config);
                 TrectextCorpusReader reader = new TrectextCorpusReader(inputPath.toString())) {

                int count = 0;
                GameDocument doc;

                while ((doc = reader.nextDocument()) != null) {
                    Document luceneDoc = new Document();
                    luceneDoc.add(new StringField("DOCNO", doc.getDocno(), Field.Store.YES));
                    luceneDoc.add(new TextField("TITLE", doc.getTitle(), Field.Store.YES));
                    luceneDoc.add(new StringField("AUTHOR", doc.getAuthor(), Field.Store.YES));
                    luceneDoc.add(new StringField("PRICE", doc.getPrice(), Field.Store.YES));
                    luceneDoc.add(new StringField("URL", doc.getUrl(), Field.Store.YES));
                    luceneDoc.add(new TextField("CONTENT", doc.getText(), Field.Store.YES));
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

    //Replace FileUtils.cleanDirectory() — standard recursive deletion
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
