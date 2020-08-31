package com.example.globalnews.Adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.globalnews.MainActivity;
import com.example.globalnews.R;
import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> implements Filterable {
    public List<News> news;
    private List<News> newsAll;
    private String queryText = "";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private ClickOnCardListener clickOnCardListener;
    private onReachEndListener onReachEndListener;
    private ClickOnShareListener clickOnShareListener;
    private ClickOnAddToFavoriteListener clickOnAddToFavoriteListener;

    public NewsAdapter() {
        news = new ArrayList<News>();
        newsAll = new ArrayList<News>();
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
        notifyDataSetChanged();
    }

    public interface ClickOnCardListener{
        void ClickOnCard(int position); //to click the poster image
    }

    public void setClickOnCardListener(ClickOnCardListener clickOnCardListener) {
        this.clickOnCardListener = clickOnCardListener;
    }
    public interface ClickOnAddToFavoriteListener{
        void ClickOnAddToFavorite(int position); //to click the poster image
    }

    public void setClickOnAddToFavoriteListener(ClickOnAddToFavoriteListener clickOnAddToFavoriteListener) {
        this.clickOnAddToFavoriteListener = clickOnAddToFavoriteListener;
    }

    public interface ClickOnShareListener{
        void ClickOnShare(int position); //to click the poster image
    }

    public void setClickOnShareListener(ClickOnShareListener clickOnShareListener) {
        this.clickOnShareListener = clickOnShareListener;
    }

    public interface onReachEndListener{
        void onReachEnd(); // method call when we come end of page
    }

    public void setOnReachEndListener(NewsAdapter.onReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }
    public void setNews(List<News> news) {
        this.news = news;
        this.newsAll = new ArrayList<>(this.news);
        notifyDataSetChanged();
    }
    public List<News> getNews() {
        return news;
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        return new NewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHolder holder, int position) {
        if ( news.size() >= 20 && position > news.size() - 2 && onReachEndListener != null){///when we come end of page and onReachEndListener not null
            onReachEndListener.onReachEnd();
        }
        News news1 = news.get(position);
        ImageView imageView = holder.imageViewNews; //get imageview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setClipToOutline(true);
        }
        if(news1.getUrlToImage() != null && !news1.getUrlToImage().isEmpty()) {
            Picasso.get().load(news1.getUrlToImage()).resize(640, 360).centerCrop().placeholder(R.drawable.download_loading).error(R.drawable.news).into(imageView); // download  image
        }else{
            Picasso.get().load(R.drawable.news).resize(640, 360).centerCrop().placeholder(R.drawable.download_loading).into(imageView); // download  image
        }
        String title = news1.getTitle();
        if (title.toLowerCase().contains(queryText.toLowerCase()) && queryText != null) {
            int start = title.toLowerCase().indexOf(queryText.toLowerCase());
            int end = start + queryText.length();
            Spannable span  = new SpannableString(title);
            span.setSpan(new BackgroundColorSpan(Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textViewTitle.setText(span);
        } else {
            holder.textViewTitle.setText(title);
        }
        TextView textViewSourceName = holder.textViewSourceName;
        TextView textViewPublishedAt = holder.textViewPublishedAt;
        String data = news1.getPublishedAt().replace("-", "/").replace("T", " ").replace("Z", "");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        long timeInMillis = date.getTime();
        textViewPublishedAt.setText(getTimeAgo(timeInMillis));

        textViewSourceName.setText(news1.getSourceName());
        ImageView imageViewFavorite = holder.imageViewFavorite;
        if(MainActivity.isFavorite(news1.getTitle())){
            imageViewFavorite.setImageResource(R.drawable.favorite_remove);
        }
        else{
            imageViewFavorite.setImageResource(R.drawable.favorite_add);
        }
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public class NewsHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewSourceName;
        private TextView textViewPublishedAt;
        private ImageView imageViewNews;
        private ImageView imageViewShare;
        private ImageView imageViewFavorite;

        public NewsHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewSourceName = itemView.findViewById(R.id.textViewSourceName);
            textViewPublishedAt = itemView.findViewById(R.id.textViewPublishedAt);
            imageViewNews = itemView.findViewById(R.id.imageViewNews);
            imageViewShare = itemView.findViewById(R.id.imageViewShare);
            imageViewFavorite = itemView.findViewById(R.id.imageViewFavorite);
            itemView.setOnClickListener(new View.OnClickListener() { // when the click image
                @Override
                public void onClick(View v) {
                    if(clickOnCardListener != null){
                        clickOnCardListener.ClickOnCard(getAdapterPosition());
                    }
                }
            });
            imageViewShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnShareListener != null){
                        clickOnShareListener.ClickOnShare(getAdapterPosition());
                    }
                }
            });
            imageViewFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnAddToFavoriteListener != null){
                        clickOnAddToFavoriteListener.ClickOnAddToFavorite(getAdapterPosition());
                    }
                }
            });
        }
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (Locale.getDefault().getLanguage().equals("ru")) {
            if (diff < MINUTE_MILLIS) {
                return "только что";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "1 мин. назад";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " мин. назад";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "1 ч. назад";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " ч. назад";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "вчера";
            } else {
                return diff / DAY_MILLIS + " дн. назад";
            }
        } else {
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        }
    }
    public void clear(){
        this.news.clear();
        notifyDataSetChanged();
    }
    //for add new ArrayList
    //we can add values this ArrayList
    public void addNews(List<News> news){
        this.news.addAll(news);
        this.newsAll = new ArrayList<>(this.news);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    Filter filter = new Filter() {
        // Run on background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<News> filteredNews = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredNews.addAll(newsAll);
            } else {
                for (News news : newsAll) {
                    if (news.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredNews.add(news);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredNews;
            return filterResults;
        }

        // Run on UI thread
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            news.clear();
            news.addAll((Collection<? extends News>) results.values);
            notifyDataSetChanged();
        }
    };
}
