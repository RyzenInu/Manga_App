package com.example.manga_app;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        TextView btnRegister = (TextView) findViewById(R.id.textViewSubtitleBtn);
        EditText editPassword = (EditText) findViewById(R.id.editTextTextPassword);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendLogin()){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else{
                    Toast.makeText(LoginActivity.this, "Couldn't Login", Toast.LENGTH_LONG).show();
                    editPassword.setText("");
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean sendLogin() {
        // Fazer a request http ao servidor.
        // Enviar no BODY da REQUEST, em formato json: { username: exemplo, password: exemplo }
        // Esperar pela resposta (await/promise).
        // O servidor responde com. { logged: false, error: erro } ou { logged: true }
        // Se recebermos logged: false, mostramos um Toast e limpamos o campo da passe (já está feito)
        // Se recebermos logged: true, redirecionamos para a main activity (já está feito)
        return true; // Temporário
    }
}