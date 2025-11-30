package com.itchsearch.util;

import com.itchsearch.model.GameDocument;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

//Reads game data from TRECTEXT format file
public class TrectextCorpusReader implements AutoCloseable {

    private BufferedReader br;
    private FileInputStream instream;
    private InputStreamReader reader;

    public TrectextCorpusReader(String filepath) throws IOException {
        instream = new FileInputStream(filepath);
        reader = new InputStreamReader(instream, StandardCharsets.UTF_8);
        br = new BufferedReader(reader);
    }

    //Read next document from TRECTEXT file, return null if end of file
    public GameDocument nextDocument() throws IOException {
        String line;
        StringBuilder textBuilder = new StringBuilder();
        String docno = "";
        String title = "";
        String author = "";
        String url = "";
        String price = "";
        boolean inDoc = false;
        boolean inText = false;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.equals("<DOC>")) {
                inDoc = true;
                docno = "";
                title = "";
                author = "";
                url = "";
                price = "";
                textBuilder.setLength(0);
                continue;
            }

            if (line.equals("</DOC>")) {
                if (inDoc && !docno.isEmpty()) {
                    return new GameDocument(
                            docno,
                            title,
                            author,
                            url,
                            textBuilder.toString().trim(),
                            price
                    );
                }
                inDoc = false;
                continue;
            }

            if (!inDoc) continue;

            if (line.startsWith("<DOCNO>") && line.endsWith("</DOCNO>")) {
                docno = extractTagContent(line, "DOCNO");
                continue;
            }

            if (line.startsWith("<TITLE>") && line.endsWith("</TITLE>")) {
                String rawTitle = extractTagContent(line, "TITLE");
                if (rawTitle.contains("$")) {
                    int idx = rawTitle.indexOf('$');
                    title = rawTitle.substring(0, idx).trim();
                    price = rawTitle.substring(idx).trim();
                } else {
                    title = rawTitle.trim();
                }
                continue;
            }

            if (line.startsWith("<AUTHOR>") && line.endsWith("</AUTHOR>")) {
                author = extractTagContent(line, "AUTHOR");
                continue;
            }

            if (line.startsWith("<URL>") && line.endsWith("</URL>")) {
                url = extractTagContent(line, "URL");
                continue;
            }

            if (line.startsWith("<TEXT>") && line.endsWith("</TEXT>")) {
                String textContent = extractTagContent(line, "TEXT");
                textBuilder.append(textContent);
                continue;
            }

            if (line.equals("<TEXT>")) {
                inText = true;
                continue;
            }

            if (line.equals("</TEXT>")) {
                inText = false;
                continue;
            }

            if (inText && !line.isEmpty()) {
                if (textBuilder.length() > 0) textBuilder.append(" ");
                textBuilder.append(line);
            }
        }

        return null;
    }

    //Extract content between XML tags
    private String extractTagContent(String line, String tagName) {
        String open = "<" + tagName + ">";
        String close = "</" + tagName + ">";
        if (line.startsWith(open) && line.endsWith(close)) {
            return line.substring(open.length(), line.length() - close.length());
        }
        return "";
    }

    @Override
    public void close() throws IOException {
        if (br != null) br.close();
        if (reader != null) reader.close();
        if (instream != null) instream.close();
    }
}