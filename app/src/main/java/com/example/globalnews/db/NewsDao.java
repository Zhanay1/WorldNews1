package com.example.globalnews.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NewsDao {
    @Query("SELECT * FROM News")
    LiveData<List<News>> getAllNews();

    @Query("SELECT * FROM favorite_news")
    LiveData<List<FavoriteNews>> getAllFavoriteNews();

    @Query("SELECT * FROM News WHERE id == :id")
    News getNewsById(int id);

    @Query("SELECT * FROM favorite_news WHERE title == :title")
    FavoriteNews getFavoriteNewsByTitle(String title);


    @Query("DELETE FROM News")
    void DeleteAllNews();

    @Insert
    void insertNews(News news);

    @Delete
    void DeleteNews(News news);

    @Insert
    void insertFavoriteNews(FavoriteNews news);

    @Delete
    void deleteFavoriteNews(FavoriteNews news);
}