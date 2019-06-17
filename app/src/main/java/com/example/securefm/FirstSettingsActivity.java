package com.example.securefm;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;

public class FirstSettingsActivity extends AppCompatActivity {
    EditText editPass, editRetryPass;
    TextView textViewAttention;
    Boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_settings);
        editPass = findViewById(R.id.editPassword);
        editRetryPass = findViewById(R.id.editRetryPassword);
        textViewAttention = findViewById(R.id.textViewAttention);
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("algorithm", "GOST-28147").commit();
        isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            textViewAttention.setText("");
        }
    }

    //Создание хеша, соли и IV
    public void onCreatePassword(View view) {
        if (!editPass.getText().toString().equals("") && !editRetryPass.getText().toString().equals("") && editPass.getText().toString().equals(editRetryPass.getText().toString())){
            try {
                //Создание и сохранения хеша в SharedPreferences
                byte[] pass = editPass.getText().toString().getBytes();
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("hashPass", new Encryption(getApplicationContext()).GetDigest(pass).toString()).commit();

                //Создание и сохранение соли
                FileOutputStream fos = openFileOutput("salt", Context.MODE_PRIVATE);
                fos.write(new Encryption(getApplicationContext()).generateSalt());
                fos.close();
                //Создание и сохранение IV для ГОСТ 28147-89 в режиме CTR
                fos = openFileOutput("IV4", Context.MODE_PRIVATE);
                fos.write(new Encryption(getApplicationContext()).generateIv(4));
                fos.close();
                //Создание и сохранение IV для ГОСТ 28147-89
                fos = openFileOutput("IV8", Context.MODE_PRIVATE);
                fos.write(new Encryption(getApplicationContext()).generateIv(8));
                fos.close();
                //Создание и сохранение IV для ГОСТ Р 34.12-2015
                fos = openFileOutput("IV16", Context.MODE_PRIVATE);
                fos.write(new Encryption(getApplicationContext()).generateIv(16));
                fos.close();

                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).commit();

                if (isFirstRun) {
                    Intent intent = new Intent(this, ChooseHomeDirActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "Пароль успешно создан", Toast.LENGTH_SHORT).show();
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("homeDir",
                            Environment.getExternalStorageDirectory().getAbsolutePath()).commit();
                } else {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "Пароль успешно изменен", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (!editPass.getText().toString().equals(editRetryPass.getText().toString())) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
        } else if (editPass.getText().toString().equals("") || editRetryPass.getText().toString().equals("")) {
            Toast.makeText(this, "Поля не могут быть пустими", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        //Отмена действия назад
    }
}
