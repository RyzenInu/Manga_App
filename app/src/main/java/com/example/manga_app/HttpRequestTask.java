package com.example.manga_app;

import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "HttpRequestTask";

    @Override
    protected String doInBackground(@NonNull String... params) {
        String urlStr = params[0];
        try {
            // Cria uma URL a partir da string fornecida
            URL url = new URL(urlStr);

            // Abre a conexão HTTP
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                // Verifica se a conexão foi bem-sucedida (código 200)
                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Lê a resposta da requisição
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    return stringBuilder.toString();
                } else {
                    // Se a conexão não foi bem-sucedida, registra o código de resposta
                    Log.e(TAG, "HTTP error code: " + urlConnection.getResponseCode());
                }
            } finally {
                // Fecha a conexão, independentemente do resultado
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error making HTTP request", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // O método chamado após a conclusão da execução em segundo plano.
        // Aqui, você pode lidar com o resultado retornado pela solicitação HTTP.
        Log.d(TAG, "HTTP response: " + result);
    }

}
