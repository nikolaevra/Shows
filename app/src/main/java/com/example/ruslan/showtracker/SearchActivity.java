package com.example.ruslan.showtracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SearchActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();
    final String search_url = "http://api.themoviedb.org/3/search/tv?api_key=ed9b90f4d390ef940882968ec0d8d677&query=%s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = (ListView) findViewById(R.id.listView2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, names);
        listView.setAdapter(adapter);
        final MenuItem myActionMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myActionMenuItem.collapseActionView();

                // remove all the spaces if there are any
                query = searchView.getQuery().toString();
                query = query.replace(" ", "%20");

                DownloadTask task = new DownloadTask();

                try {
                    String result = task.execute(String.format(search_url, query)).get();

                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = new JSONArray(jsonObject.getString("results"));

                    for (int i = 0; i < jsonArray.length(); i ++) {
                        JSONObject JTemp = jsonArray.getJSONObject(i);

                        names.add(JTemp.getString("name"));
                        ids.add(JTemp.getString("id"));
                    }
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }

                adapter.notifyDataSetChanged();
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        Intent intent = new Intent (getApplicationContext(),
                                ShowPopup.class);
                        intent.putExtra("show_id", ids.get(position));
                        startActivity(intent);
                    }
                });
                return true;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onPause() {
        finish();
        super.onPause();
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
}
