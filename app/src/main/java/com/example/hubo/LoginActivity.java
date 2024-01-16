package com.example.hubo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick(v);
            }
        });

    }
    public void onLoginClick(View view) {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();


        // Simple validation (you should implement proper validation)
        if (user.equals("zulfikar") && pass.equals("123456")) {
            // Login successful, switch to MainActivity
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            password.setText("");
            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
        }
    }
}

