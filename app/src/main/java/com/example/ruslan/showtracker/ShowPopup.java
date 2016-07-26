package com.example.ruslan.showtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ShowPopup extends AppCompatActivity {


    String show_id;
    TextView title, description, genresView, firstDateView, lastDateView,
            networksView, runTimeView;
    HashMap<String, String> extracted;
    ImageView posterView;
    RatingBar ratingBar;
    final String show_id_url = "http://api.themoviedb.org/3/tv/%s?api_key=ed9b90f4d390ef940882968ec0d8d677";
    final String poster_url = "http://image.tmdb.org/t/p/w300%s";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_popup);

        // get the intent
        Intent intent = getIntent();
        show_id = intent.getStringExtra("show_id");

        // setup the views
        title = (TextView) findViewById(R.id.showName);
        description = (TextView) findViewById(R.id.showDesc);
        firstDateView = (TextView) findViewById(R.id.airDate);
        lastDateView = (TextView) findViewById(R.id.lastAirDate);
        networksView = (TextView) findViewById(R.id.networksView);
        runTimeView = (TextView) findViewById(R.id.runTimeView);
        genresView = (TextView) findViewById(R.id.genresView);
        posterView = (ImageView) findViewById(R.id.posterSpot);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute(String.format(show_id_url, show_id)).get();
        } catch (InterruptedException | ExecutionException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT)
                    .show();
        }

        Parser helpers = new Parser();
        helpers.parse_values(result);
        extracted = helpers.get_everything();

        description.setText(extracted.get("description"));
        description.setMovementMethod(new ScrollingMovementMethod());
        title.setText(extracted.get("name"));
        ratingBar.setRating(Float.parseFloat(extracted.get("rating")) / 2);
        firstDateView.setText(String.format("First air date: %s",
                extracted.get("air_begin_date")));
        genresView.setText(String.format("Genres: %s", extracted.get("genres")));
        networksView.setText(String.format("Networks: %s", extracted.get("networks")));
        lastDateView.setText(String.format("Last air date: %s", extracted.get("air_last_date")));
        runTimeView.setText(String.format("Run Time: %s mins", extracted.get("run_time")));

        ImageDownloader imageTask = new ImageDownloader();
        if (extracted.get("poster") != null) {
            try {
                imageTask.execute(String.format(poster_url, extracted.get("poster")));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            posterView.setBackgroundResource(R.drawable.nopicture);
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream input = connection.getInputStream();

                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            posterView.setImageBitmap(bitmap);
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char cur = (char) data;
                    result += cur;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite_heart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                if (!inSet(MainActivity.favoritesIDs, show_id)) {
                    MainActivity.favorites.add(extracted.get("name"));
                    MainActivity.favoritesIDs.add(show_id);
                    save();
                    MainActivity.adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(),
                            String.format("Added %s to your collection",
                                    extracted.get("name")),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "The show is already in your collection",
                            Toast.LENGTH_SHORT).show();
                }
                return true;

            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean inSet(Set<String> list, String show_id) {
        if (list != null) {
            for (String fav : list) {
                if (Objects.equals(fav, show_id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void save() {
        SharedPreferences settings = getSharedPreferences(
                "com.example.ruslan.showtracker", Context.MODE_PRIVATE);
        settings.edit().putStringSet("favorites", MainActivity.favorites).apply();
        settings.edit().putStringSet("favoritesIDs", MainActivity.favoritesIDs).apply();
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

}
