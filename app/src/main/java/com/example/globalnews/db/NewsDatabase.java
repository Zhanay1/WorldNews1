package com.example.globalnews.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {News.class, FavoriteNews.class}, version = 3, exportSchema = false)
public abstract class NewsDatabase extends RoomDatabase {
    private static final String DB_NAME = "news.db";
    private static NewsDatabase database;
    private static final Object LOCK = new Object();

    public static NewsDatabase getInstance(Context context){
        synchronized (LOCK) {
            if (database == null) {
                database = Room.databaseBuilder(context, NewsDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
            }
        }
        return database;
    }
    public abstract NewsDao newsDao();
}
