package com.example.manga_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String LOGIN_URL = "http://10.0.2.2:2000/user/login/";
    private static final String VERIFY_USER_URL = "http://10.0.2.2:2000/user/";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se o usuário já está logado
        if (isUserLoggedIn()) {
            redirectToMainActivity();
            return; // Encerra o método onCreate se o usuário já estiver logado
        }

        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView btnRegister = findViewById(R.id.textViewSubtitleBtn);

        btnLogin.setOnClickListener(v -> {
            EditText editUsername = findViewById(R.id.editTextTextEmail);
            EditText editPassword = findViewById(R.id.editTextTextPassword);
            String username = editUsername.getText().toString();
            String password = editPassword.getText().toString();

            sendLogin(username, password);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean isUserLoggedIn() {
        // Verificar se o usuário está logado consultando as SharedPreferences
        return getSharedPreferences("user_pref", MODE_PRIVATE)
                .getString("userId", null) != null;
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
                .url(LOGIN_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleNetworkError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
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
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show());
    }

    private void handleLoginResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String responseBody = response.body().string();
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            boolean logged = jsonResponse.optBoolean("logged");
            if (logged) {
                String userId = jsonResponse.optString("userId");
                saveUserIdToSharedPreferences(userId);
                verifyUserLoginStatus(userId);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Autenticação falhou", Toast.LENGTH_SHORT).show();
                    clearSharedPreferences();
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
                .url(VERIFY_USER_URL + userId)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleNetworkError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    handleLoginStatusResponse(response);
                } catch (IOException e) {
                    handleNetworkError(e);
                }
            }
        });
    }

    private void handleLoginStatusResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            handleNetworkError(new IOException("Unexpected code " + response));
            return;
        }

        String responseBody = response.body().string();
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            boolean loggedIn = jsonResponse.optBoolean("loggedIn");

            if (loggedIn) {
                redirectToMainActivity();
            } else {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Autenticação falhou", Toast.LENGTH_SHORT).show());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void redirectToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void clearSharedPreferences() {
        getSharedPreferences("user_pref", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
