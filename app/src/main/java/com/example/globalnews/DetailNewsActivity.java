package com.example.globalnews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.example.globalnews.db.NewsViewModel;
import com.squareup.picasso.Picasso;

public class DetailNewsActivity extends AppCompatActivity {

    private TextView textViewSourceName;
    private TextView textViewAuthor;
    private TextView textViewTitle;
    private TextView textViewContent;
    private TextView textViewMore;
    private ImageView imageViewNews;
    private WebView webView;

    private NewsViewModel newsViewModel;
    private News news;
    private int id;
    private String title;
    private String sourceName;
    private String author;
    private String content;
    private String url;
    private String urlToImage;
    private String description;
    private String publishedAt;
    private Toolbar toolbar;
    private Switch switchDark;
    SharedPref mySharedPref;
    private static int currentTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mySharedPref = new SharedPref(this);
        if(mySharedPref.loadNightModeState() == true){
            setTheme(R.style.DarkTheme);
            currentTheme = R.style.DarkTheme;
        }else{
            setTheme(R.style.AppTheme);
            currentTheme = R.style.AppTheme;
        }

        setContentView(R.layout.activity_detail_news);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setCustomView(R.layout.action_bar);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewSourceName = findViewById(R.id.textViewSourceName);
        textViewMore = findViewById(R.id.textViewMore);
        textViewContent = findViewById(R.id.textViewContent);
        imageViewNews = findViewById(R.id.imageViewNews);


        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("title")){
            title = intent.getStringExtra("title");
            sourceName = intent.getStringExtra("sourceName");
            author = intent.getStringExtra("author");
            content = intent.getStringExtra("content");
            url = intent.getStringExtra("url");
            urlToImage = intent.getStringExtra("urlToImage");
            publishedAt = intent.getStringExtra("published");
            description = intent.getStringExtra("description");
        }
        else {
            finish();
        }
//        news = newsViewModel.getNewsById(id);
        textViewTitle.setText(title);
        if(author.equals("null") || author.isEmpty()){
            textViewAuthor.setText(R.string.unknown);
        }
        else {
            textViewAuthor.setText(author);
        }
        textViewMore.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailNewsActivity.this, WebViewActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("sourceName", sourceName);
                intent.putExtra("author", author);
                intent.putExtra("urlToImage", urlToImage);
                intent.putExtra("content", content);
                intent.putExtra("url", url);
                intent.putExtra("published", publishedAt);
                intent.putExtra("description", description);
                startActivity(intent);
            }
        });
        if(description.equals("null") || description.isEmpty()) {
            textViewContent.setText("");
        }else{
            textViewContent.setText(description.replaceAll("\\[[^{}]*]", "").trim().replaceAll("\\s+", " "));
        }
        textViewSourceName.setText(sourceName);
        if(urlToImage != null && !urlToImage.isEmpty()) {
            Picasso.get().load(R.drawable.news).resize(640, 360).centerInside().placeholder(R.drawable.download_loading).error(R.drawable.news).into(imageViewNews); // download  image
        }else{
            Picasso.get().load(urlToImage).resize(640, 360).placeholder(R.drawable.download_loading).into(imageViewNews); // download  image
        }          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageViewNews.setClipToOutline(true);
        }
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite, menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(this.title);
        if (favoriteNews == null) {
            menu.findItem(R.id.Favorites).setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_white));
        } else {
            menu.findItem(R.id.Favorites).setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_remove));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mShare) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = url;
            String ShareSub = title;
            intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
        }
        else if (id == R.id.Favorites) {
            FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(this.title);
            if(favoriteNews == null){
                newsViewModel.insertFavoriteNews(new FavoriteNews(sourceName, author, title, description, url, urlToImage, publishedAt, content));
                item.setIcon(R.drawable.favorite_remove);
                Toast.makeText(this, R.string.add_to_favorites, Toast.LENGTH_SHORT).show();
            }
            else{
                newsViewModel.deleteFavoriteNews(favoriteNews);
                item.setIcon(R.drawable.favorite_white);
                Toast.makeText(this, R.string.delete_of_favorites, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        if ((currentTheme != R.style.DarkTheme && mySharedPref.loadNightModeState()) || (currentTheme == R.style.DarkTheme && !mySharedPref.loadNightModeState())) {
            recreate();
        }
        super.onResume();
        invalidateOptionsMenu();
    }
}