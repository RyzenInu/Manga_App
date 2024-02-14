package com.example.manga_app;

import android.content.Intent;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.*;

public class LoginActivity extends AppCompatActivity {
    private static final String URL = "http://192.168.137.1:3000/";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String TAG = "LOGIN_FRAGMENT";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Verifica se o usuário já está logged in
        if (isUserLoggedIn()) {
            verifyUserLoginStatus(getSharedPreferences("user_pref", MODE_PRIVATE).getString("userId", null));
            return;
        }

        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView btnRegister = findViewById(R.id.textViewSubtitleBtn);

        btnLogin.setOnClickListener(v -> {
            EditText editUsername = findViewById(R.id.editTextLoginUsername);
            EditText editPassword = findViewById(R.id.editTextLoginPassword);
            String username = editUsername.getText().toString();
            String password = editPassword.getText().toString();

            if (!username.equals("") && !password.equals("")) {
                sendLogin(username, password);
            } else {
                Toast.makeText(this, R.string.error_login_empty_fields, Toast.LENGTH_LONG).show();
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
        });
    }

    private boolean isUserLoggedIn() {
        // Verifica se o usuário está logado consultando as SharedPreferences
        return getSharedPreferences("user_pref", MODE_PRIVATE).getString("userId", null) != null;
    }

    private void sendLogin(String username, String password) {
        JSONObject jsonLogin = new JSONObject();
        try {
            jsonLogin.put("username", username);
            jsonLogin.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonLogin.toString(), MEDIA_TYPE_JSON);

        Request request = new Request.Builder()
                .url(URL + "user/login")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleNetworkError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    handleLoginResponse(response);
                } catch (IOException e) {
                    handleNetworkError(e);
                }
            }
        });
    }

    private void handleNetworkError(IOException e) {
        e.printStackTrace();
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, R.string.error_login_auth_failed, Toast.LENGTH_SHORT).show());
    }

    private void handleLoginResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        assert response.body() != null;
        String responseBody = response.body().string();
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            boolean logged = jsonResponse.optBoolean("logged");
            if (logged) {
                String userId = jsonResponse.optString("userId");
                saveUserIdToSharedPreferences(userId);
                redirectToMainActivity();
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, R.string.error_login_auth_failed, Toast.LENGTH_LONG).show();
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveUserIdToSharedPreferences(String userId) {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .putString("userId", userId)
                .apply();
    }

    private void verifyUserLoginStatus(String userId) {
        Request request = new Request.Builder()
                .url(URL + "user/" + userId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleNetworkError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    ResponseBody responseBody = response.body();
                    JSONObject jsonObj = new JSONObject(responseBody.string());
                    if (jsonObj.has("username")) {
                        redirectToMainActivity();
                        finish();
                    } else {
                        clearSharedPreferences();
                    }
                } catch (IOException e) {
                    handleNetworkError(e);
                } catch (JSONException e) {
                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                }
            }
        });
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
    }

    private void clearSharedPreferences() {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
