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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {

    private static final String SERVER_URL = "http://89.115.17.17:3000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView btnLogin = findViewById(R.id.textViewSubtitleBtn);

        btnRegister.setOnClickListener(v -> {
            if (sendRegister()) {
                // Se o registro for bem-sucedido, a LoginActivity será iniciada
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(RegisterActivity.this, "Couldn't Register", Toast.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.zoom_in_from_center, R.anim.zoom_out_to_center);
        });
    }

    private boolean sendRegister() {
        String urlRegister = SERVER_URL + "user/create/";

        EditText editUsername = findViewById(R.id.editTextRegUsername);
        EditText editFirstname = findViewById(R.id.editTextRegFirstname);
        EditText editLastname = findViewById(R.id.editTextRegLastname);
        EditText editEmail = findViewById(R.id.editTextRegEmail);
        EditText editPassword = findViewById(R.id.editTextRegPassword);
        EditText editRepeatPassword = findViewById(R.id.editTextRegRepeatPassword);

        String username = editUsername.getText().toString();
        String firstname = editFirstname.getText().toString();
        String lastname = editLastname.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String rpassword = editRepeatPassword.getText().toString();

        // If passwords don't match
        if(!password.equals(rpassword)){
            return false;
        }

        // JSON
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

        // Executaa a tarefa assíncrona para a solicitação HTTP
        new SendRegisterTask(this).execute(urlRegister, jsonRegister.toString());

        return true;
    }

    private static class SendRegisterTask extends AsyncTask<String, Void, String> {
        private WeakReference<RegisterActivity> activityReference;

        public SendRegisterTask(RegisterActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            String urlRegister = params[0];
            String jsonRegisterData = params[1];

            try {
                URL url = new URL(urlRegister);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Enviar dados JSON no corpo da solicitação
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonRegisterData.getBytes());
                outputStream.flush();
                outputStream.close();

                // Ler a resposta do servidor
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    response.append(line);
                }
                bufferedReader.close();
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            RegisterActivity registerActivity = activityReference.get();
            if (registerActivity != null) {
                if (response != null) {
                    try {
                        // Analisar a resposta JSON do servidor
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean registered = jsonResponse.optBoolean("registered");
                        if (registered) {
                            // Registro bem-sucedido
                            // Iniciar a LoginActivity
                            Intent intent = new Intent(registerActivity, LoginActivity.class);
                            registerActivity.startActivity(intent);
                            registerActivity.finish(); // Encerrar a atividade de registro

                        } else {
                            // Mostrar um Toast se o registro falhou
                            Toast.makeText(registerActivity, "Registro falhou", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
