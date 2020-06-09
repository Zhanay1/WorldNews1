package com.example.globalnews.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class NetworkUtils {
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines"; // request to network
    private static final String BASE_SEARCH_URL = "https://newsapi.org/v2/everything"; // request to network


    //parameters from url
    private static final String PARAMS_API_KEY = "apiKey";
    private static final String PARAMS_PAGE = "page";
    private static final String PARAMS_COUNTRY = "country";
    private static final String PARAMS_CATEGORY = "category";
    private static final String PARAMS_SORT_BY = "sortBy";
    private static final String PARAMS_Q = "q";

    //values from parameters

    private static final String API_KEY = "7927dc2284db4d77bc38255c4c873684";
    private static final String COUNTRY = "us";
    private static final String PUBLISHED_AT = "publishedAt";

    public static URL buildURl(int page, String category){
        URL result = null;
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAMS_COUNTRY, COUNTRY)
                .appendQueryParameter(PARAMS_CATEGORY, category)
                .appendQueryParameter(PARAMS_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAMS_API_KEY, API_KEY).build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static URL buildSearchURl(String q, int page){
        URL result = null;
        Uri uri = Uri.parse(BASE_SEARCH_URL).buildUpon()
                .appendQueryParameter(PARAMS_Q, q)
                .appendQueryParameter(PARAMS_SORT_BY, PUBLISHED_AT)
                .appendQueryParameter(PARAMS_PAGE, String.valueOf(page))
                .appendQueryParameter(PARAMS_API_KEY, API_KEY).build();
        try {
            result = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result;
    }



    public static JSONObject getJSONFromNetwork (int page, String category){  //get JSON data from network
        JSONObject result = null;
        URL url = buildURl(page, category); // get variables
        try {
            result = new JSONLoadTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class JSONLoader extends AsyncTaskLoader<JSONObject> {

        private Bundle bundle;

        private OnStartLoadingListener onStartLoadingListener;

        public interface OnStartLoadingListener{
            void OnStartLoading();
        }

        public void setOnStartLoadingListener(OnStartLoadingListener onStartLoadingListener) {
            this.onStartLoadingListener = onStartLoadingListener;
        }

        public JSONLoader(@NonNull Context context, Bundle bundle) {
            super(context);
            this.bundle = bundle;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if(onStartLoadingListener != null){
                onStartLoadingListener.OnStartLoading();
            }
            forceLoad();
        }

        @Nullable
        @Override
        public JSONObject loadInBackground() {
            if(bundle == null){
                return null;
            }
            String urlAsString = bundle.getString("url");
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            JSONObject result = null;
            if (url == null) {
                return result;
            }
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null){ // if line not null we add line to builder
                    builder.append(line);
                    line = bufferedReader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                if (httpURLConnection != null){
                    httpURLConnection.disconnect(); // disconnect
                }
            }
            return result;
        }
    }

    private static class JSONLoadTask extends AsyncTask<URL,Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(URL... urls) {
            JSONObject result = null;
            if (urls == null || urls.length == 0) {
                return result;
            }
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) urls[0].openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null){ // if line not null we add line to builder
                    builder.append(line);
                    line = bufferedReader.readLine();
                }
                result = new JSONObject(builder.toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                if(httpURLConnection != null){
                    httpURLConnection.disconnect();
                }
            }
            return result;
        }
    }
}
