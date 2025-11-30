package com.itchsearch.model;

public class Game {
    private String title;
    private String author;
    private String price;
    private String url;
    private String fullDescription;
    private String highlightedDescription;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public String getHighlightedDescription() {
        return highlightedDescription;
    }

    public void setHighlightedDescription(String highlightedDescription) {
        this.highlightedDescription = highlightedDescription;
    }
}