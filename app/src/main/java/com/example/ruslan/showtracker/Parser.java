package com.example.ruslan.showtracker;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by ruslan on 17/07/16.
 */
public class Parser extends AppCompatActivity {

    HashMap<String, String> extractedValues = new HashMap<>();

    public HashMap<String, String> get_everything() {
        return this.extractedValues;
    }

    public String get(String key) {
        return this.extractedValues.get(key);
    }

    public void parse_values(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            this.extractedValues.put("description", jsonObject.getString("overview"));
            this.extractedValues.put("name", jsonObject.getString("original_name"));
            this.extractedValues.put("poster", jsonObject.getString("poster_path"));
            this.extractedValues.put("air_begin_date", jsonObject.getString("first_air_date"));
            this.extractedValues.put("rating", jsonObject.getString("vote_average"));
            this.extractedValues.put("air_last_date", jsonObject.getString("last_air_date"));
            String extracted_genres = "";
            String extracted_networks = "";
            String extracted_run_time = "";

            JSONArray tempArr = new JSONArray(jsonObject.getString("genres"));
            for (int i = 0; i < tempArr.length(); i++) {
                JSONObject tempObj = new JSONObject(tempArr.getString(i));
                extracted_genres += tempObj.getString("name");
                if (tempArr.length() > 1)
                    extracted_genres += ", ";
            }

            tempArr = new JSONArray(jsonObject.getString("networks"));
            for (int i = 0; i < tempArr.length(); i++) {
                JSONObject tempObj = new JSONObject(tempArr.getString(i));
                extracted_networks += tempObj.getString("name");
                if (tempArr.length() > 1)
                    extracted_networks += ", ";
            }

            tempArr = new JSONArray(jsonObject.getString("episode_run_time"));
            for (int i = 0; i < tempArr.length(); i++) {
                extracted_run_time += tempArr.getString(i);
                if (tempArr.length() > 1 && tempArr.length() != (i + 1))
                    extracted_run_time += ", ";
            }
            this.extractedValues.put("genres", extracted_genres);
            this.extractedValues.put("networks", extracted_networks);
            this.extractedValues.put("run_time", extracted_run_time);

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast
                    .LENGTH_LONG).show();
        }
    }
}

