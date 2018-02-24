package com.models;

import javax.persistence.*;

@Entity
@Table(name = "news")
public class SearchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    @Column(name = "category")
    private int category;

    private String categoryText;

    @Column(name = "timestamp")
    private int timestamp;

    @Column(name = "content")
    private String content;

    public SearchResult() {

    }

    public String getCategoryText() {
        return categoryText;
    }

    public void setCategoryText(String categoryText) {
        this.categoryText = categoryText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SearchResult(String title, String url, int category, int timestamp, String content) {
        this.title = title;
        this.url = url;
        this.category = category;
        this.timestamp = timestamp;
        this.content = content;
    }
}
