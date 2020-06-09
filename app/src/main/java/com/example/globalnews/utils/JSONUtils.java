package com.example.globalnews.utils;



import com.example.globalnews.db.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONUtils {
    private static final String KEY_ARTICLES = "articles";
    //objects in results
    private static final String KEY_SOURCE = "source";
    private static final String KEY_NAME = "name";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";
    private static final String KEY_URL_TO_IMAGE = "urlToImage";
    private static final String KEY_PUBLISHED_AT = "publishedAt";
    private static final String KEY_CONTENT = "content";


    public static List<News> getNewsFromJSON(JSONObject jsonObject){ //  we get array movies
        List<News> result = new ArrayList<>();
        if(jsonObject == null){
            return result;
        }
        try {
            JSONArray jsonArray = jsonObject.getJSONArray(KEY_ARTICLES);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject objectNews = jsonArray.getJSONObject(i);
                String sourceName = objectNews.getJSONObject(KEY_SOURCE).getString(KEY_NAME);
                String author = objectNews.getString(KEY_AUTHOR);
                String title = objectNews.getString(KEY_TITLE);
                String description = objectNews.getString(KEY_DESCRIPTION);
                String url = objectNews.getString(KEY_URL);
                String urlToImage = objectNews.getString(KEY_URL_TO_IMAGE);
                String publishedAt = objectNews.getString(KEY_PUBLISHED_AT);
                String content = objectNews.getString(KEY_CONTENT);
                // make object to get data from class News
                description = description.replaceAll("\\<.*?\\>", "");
                News news = new News(sourceName, author, title, description, url, urlToImage, publishedAt, content);
                result.add(news);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
