package com.itchsearch.model;

//Game document model mapped from TRECTEXT format
public class GameDocument {
    private String docno;
    private String title;
    private String author;
    private String url;
    private String text;
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
    public void setAutor(String author) { this.author = nullToEmpty(author); }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = nullToEmpty(url); }

    public String getText() { return text; }
    public void setText(String text) { this.text = nullToEmpty(text); }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = nullToEmpty(price); }

    //Convert null to empty string
    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}