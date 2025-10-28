package com.example.demo.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.PersistenceCreator;

@Table("articles")
public class ArticleEntity implements Persistable<String> {
    @Id
    private String doi;
    private String title;
    private String authors;
    private String published;
    @Column("peer_reviewed")
    private Boolean peerReviewed;

    @Transient
    private boolean isNew;

    public ArticleEntity() {
        this.isNew = true;
    }

    public ArticleEntity(String doi, String title, String authors, String published, Boolean peerReviewed) {
        this.doi = doi;
        this.title = title;
        this.authors = authors;
        this.published = published;
        this.peerReviewed = peerReviewed;
        this.isNew = true;
    }

    @PersistenceCreator
    public ArticleEntity(String doi, String title, String authors, String published, Boolean peerReviewed, boolean isNew) {
        this.doi = doi;
        this.title = title;
        this.authors = authors;
        this.published = published;
        this.peerReviewed = peerReviewed;
        this.isNew = false;
    }

    @Override
    public String getId() {
        return doi;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }

    public String getDoi() { return doi; }
    public void setDoi(String doi) { this.doi = doi; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getPublished() { return published; }
    public void setPublished(String published) { this.published = published; }
    public Boolean getPeerReviewed() { return peerReviewed; }
    public void setPeerReviewed(Boolean peerReviewed) { this.peerReviewed = peerReviewed; }
}
