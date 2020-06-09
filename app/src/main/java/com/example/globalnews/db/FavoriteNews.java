package com.example.globalnews.db;

import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "favorite_news")
public class FavoriteNews extends News {
    public FavoriteNews(int id, String sourceName, String author, String title, String description, String url, String urlToImage, String publishedAt, String content) {
        super(id, sourceName, author, title, description, url, urlToImage, publishedAt, content);
    }

    @Ignore
    public FavoriteNews(News news){
        super(news.getId(), news.getSourceName(), news.getAuthor(), news.getTitle(), news.getDescription(), news.getUrl(),
                news.getUrlToImage(), news.getPublishedAt(), news.getContent());
    }

    @Ignore
    public FavoriteNews(String sourceName, String author, String title, String description, String url, String urlToImage, String publishedAt, String content) {
        super(sourceName, author, title, description, url, urlToImage, publishedAt, content);
    }
}
