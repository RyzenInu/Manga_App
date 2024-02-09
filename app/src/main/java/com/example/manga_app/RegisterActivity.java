package com.example.manga_app;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.*;

public class RegisterActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://89.115.17.17:3000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView btnLogin = findViewById(R.id.textViewSubtitleBtn);

        btnRegister.setOnClickListener(v -> {
            if (checkFields()) {
                if (sendRegister()) {
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't Register", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
        });
    }

    private boolean checkFields() {
        EditText[] fields = {
                findViewById(R.id.editTextRegUsername),
                findViewById(R.id.editTextRegFirstname),
                findViewById(R.id.editTextRegLastname),
                findViewById(R.id.editTextRegEmail),
                findViewById(R.id.editTextRegPassword),
                findViewById(R.id.editTextRegRepeatPassword)
        };

        for (EditText field : fields) {
            if (field.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean sendRegister() {
        EditText editUsername = findViewById(R.id.editTextRegUsername);
        EditText editFirstname = findViewById(R.id.editTextRegFirstname);
        EditText editLastname = findViewById(R.id.editTextRegLastname);
        EditText editEmail = findViewById(R.id.editTextRegEmail);
        EditText editPassword = findViewById(R.id.editTextRegPassword);

        String username = editUsername.getText().toString();
        String firstname = editFirstname.getText().toString();
        String lastname = editLastname.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String rpassword = ((EditText) findViewById(R.id.editTextRegRepeatPassword)).getText().toString();

        if (!password.equals(rpassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return false;
        }

        JSONObject jsonRegister = new JSONObject();
        try {
            jsonRegister.put("username", username);
            jsonRegister.put("firstname", firstname);
            jsonRegister.put("lastname", lastname);
            jsonRegister.put("email", email);
            jsonRegister.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendRegisterTask(this).execute(SERVER_URL + "user/create/", jsonRegister.toString());

        return true;
    }

    private static class SendRegisterTask extends AsyncTask<String, Void, Boolean> {
        private WeakReference<RegisterActivity> activityReference;

        public SendRegisterTask(RegisterActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String urlRegister = params[0];
            String jsonRegisterData = params[1];

            OkHttpClient client = new OkHttpClient();

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRegisterData);
            Request request = new Request.Builder()
                    .url(urlRegister)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    return jsonResponse.optBoolean("registered");
                } else {
                    return false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean registered) {
            RegisterActivity registerActivity = activityReference.get();
            if (registerActivity != null) {
                if (registered) {
                    Toast.makeText(registerActivity, "Registration successful", Toast.LENGTH_SHORT).show();
                    registerActivity.startActivity(new Intent(registerActivity, LoginActivity.class));
                } else {
                    Toast.makeText(registerActivity, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
