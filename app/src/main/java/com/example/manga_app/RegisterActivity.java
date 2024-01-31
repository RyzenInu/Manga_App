package com.example.manga_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        TextView btnLogin = findViewById(R.id.textViewSubtitleBtn);

        btnRegister.setOnClickListener(v -> {
            if(sendRegister()){
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            } else{
                Toast.makeText(RegisterActivity.this, "Couldn't Register", Toast.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private boolean sendRegister() {
        // Fazer a request http ao servidor.
        // Enviar no BODY da REQUEST, em formato json: { username: exemplo, firstname: exemplo, lastname: exemplo, email: exemplo, password: exemplo  }
        // Esperar pela resposta (await/promise).
        // O servidor responde com. { registered: false, error: erro } ou { registered: true }
        // Se recebermos registered: false, mostramos um Toast (DONE)
        // Se recebermos registered: true, redirecionamos para a login activity (DONE)
        return true; // Temporário
    }
}