package com.example.manga_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProfileActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private static final String SERVER_URL = "http://192.168.1.2:3000/";
    private static final String TAG = "PROFILE_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton btnBack = findViewById(R.id.btnProfileBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            }
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSharedPreferences();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
                finish();
            }
        });

        ImageView imageView = findViewById(R.id.imgViewProfile);
        TextView firstLastName = findViewById(R.id.txtViewFirstLastName);
        TextView username = findViewById(R.id.txtViewUsername);
        TextView email = findViewById(R.id.txtViewEmail);
        TextView lab = findViewById(R.id.txtViewLab);

        SharedPreferences sharedPref = getSharedPreferences("user_pref", Context.MODE_PRIVATE);
        String userId = String.valueOf(sharedPref.getString("userId", ""));
        Request request = new Request.Builder().url(SERVER_URL + "user/" + userId).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error getting items from server.");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    String responseString = responseBody.string();
                    Log.d(TAG, responseString);
                    try {
                        JSONObject jsonObj = new JSONObject(responseString);
                        runOnUiThread(() -> {
                            try {
                                String name = jsonObj.getString("firstname") + " " + jsonObj.getString("lastname");
                                firstLastName.setText(name);
                                username.setText(jsonObj.getString("username"));
                                email.setText(jsonObj.getString("email"));
                                lab.setText(jsonObj.getString("lab"));
                                Picasso.get().load(SERVER_URL + "images/users/" + jsonObj.getString("img")).into(imageView);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error retriving values from json body: " + e.getMessage());
                            }
                        });
                    } catch (JSONException e) {
                        throw new IOException("Unexpected format", e);
                    }
                }
            }

        });
    }
    private void clearSharedPreferences() {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}