package com.example.securefm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Проверка на первый запуск приложения
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            Intent intent = new Intent(this, FirstSettingsActivity.class);
            startActivity(intent);
        }
    }

    public void buttonLogin(View view) {
        EditText passText = findViewById(R.id.passText);
        String digest = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("hashPass", "");
        //Проверка пароля по хешу
        if (digest.equals(new Encription().GetDigest(passText.getText().toString().getBytes()).toString())) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("PASSWORD", passText.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        //Отмена действия назад
    }
}
