package com.example.manga_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
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
        });
    }

    private boolean sendRegister() {
        String urlRegister = "http://10.0.2.2:2000/user/create/";
        String username = "editTextRegUsername";
        String firstname = "editTextRegFirstname";
        String lastname = "editTextRegLastname";
        String email = "editTextRegEmail";
        String password = "editTextRegPassword";
        String rpassword = "editTextRegRepeatPassword";

        // JSON
        JSONObject jsonRegister = new JSONObject();
        try {
            jsonRegister.put("username", username);
            jsonRegister.put("firstname", firstname);
            jsonRegister.put("lastname", lastname);
            jsonRegister.put("email", email);
            jsonRegister.put("password", password);
            jsonRegister.put("rpassword", rpassword);
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
