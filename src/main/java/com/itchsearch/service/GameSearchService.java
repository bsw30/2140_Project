package com.itchsearch.service;

import com.itchsearch.model.Game;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Lucene search service for games.
 */
@Service
public class GameSearchService {

    private static final String INDEX_DIR = "data/index";

    public List<Game> searchGames(String queryStr, int maxResults) {
        List<Game> results = new ArrayList<>();
        Path indexPath = Paths.get(System.getProperty("user.dir")).resolve(INDEX_DIR);

        try (FSDirectory dir = FSDirectory.open(indexPath);
             DirectoryReader reader = DirectoryReader.open(dir)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            String[] searchFields = {"TITLE", "AUTHOR", "CONTENT"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(searchFields, new StandardAnalyzer());
            Query query = parser.parse(queryStr);

            TopDocs topDocs = searcher.search(query, maxResults);
            for (ScoreDoc sd : topDocs.scoreDocs) {
                Document doc = searcher.doc(sd.doc);
                Game game = new Game();
                game.setTitle(doc.get("TITLE"));
                game.setAuthor(doc.get("AUTHOR"));
                game.setPrice(doc.get("PRICE"));
                game.setUrl(doc.get("URL"));
                game.setFullDescription(doc.get("CONTENT"));
                results.add(game);
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return results;
    }
}
