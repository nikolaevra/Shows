package com.example.ruslan.showtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    static Set<String> favorites = new HashSet<>();
    static Set<String> favoritesIDs = new HashSet<>();
    static ArrayAdapter<String> mainAdapter;
    ArrayList<String> valuesForAdapter = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView1);

        SharedPreferences settings = getSharedPreferences(
                "com.example.ruslan.showtracker", Context.MODE_PRIVATE);
        favorites = settings.getStringSet("favorites", new HashSet<String>());
        favoritesIDs = settings.getStringSet("favoritesIDs", new HashSet<String>());

        valuesForAdapter.clear();
        valuesForAdapter.addAll(favorites);

        mainAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, valuesForAdapter);
        listView.setAdapter(mainAdapter);
        mainAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        SharedPreferences settings = getSharedPreferences(
                "com.example.ruslan.showtracker", Context.MODE_PRIVATE);
        settings.edit().putStringSet("favorites", favorites).apply();
        settings.edit().putStringSet("favoritesIDs", favoritesIDs).apply();
        super.onStop();
    }

    @Override
    protected void onResume() {
        valuesForAdapter.clear();
        valuesForAdapter.addAll(favorites);

        mainAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, valuesForAdapter);
        listView.setAdapter(mainAdapter);
        mainAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
        }
        return true;
    }
}
