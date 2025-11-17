package com.itchsearch.model;

/**
 * Minimal game document model mapped from TRECTEXT.
 * Fields:
 *  - docno (internal id)
 *  - title (TITLE tag without price suffix)
 *  - author (AUTHOR)
 *  - url (URL)
 *  - text (fullDescription from TEXT)
 *  - price (price part parsed from TITLE, e.g., "$3.99-50%"; empty if none)
 */
public class GameDocument {
    private String docno;
    private String title;
    private String author;
    private String url;
    private String text;   // fullDescription
    private String price;

    public GameDocument() {}

    public GameDocument(String docno, String title, String author, String url, String text, String price) {
        this.docno = nullToEmpty(docno);
        this.title = nullToEmpty(title);
        this.author = nullToEmpty(author);
        this.url = nullToEmpty(url);
        this.text = nullToEmpty(text);
        this.price = nullToEmpty(price);
    }

    public String getDocno() { return docno; }
    public void setDocno(String docno) { this.docno = nullToEmpty(docno); }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = nullToEmpty(title); }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = nullToEmpty(author); }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = nullToEmpty(url); }

    /** Full description from <TEXT> */
    public String getText() { return text; }
    public void setText(String text) { this.text = nullToEmpty(text); }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = nullToEmpty(price); }

    // --- helpers ---
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}