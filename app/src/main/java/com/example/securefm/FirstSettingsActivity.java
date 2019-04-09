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
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("hashPass", new Encription().GetDigest(pass).toString()).commit();

                //Создание и сохранение соли
                FileOutputStream fos = openFileOutput("salt", Context.MODE_PRIVATE);
                fos.write(new Encription().generateSalt());
                fos.close();
                //Создание и сохранение IV
                fos = openFileOutput("IV", Context.MODE_PRIVATE);
                fos.write(new Encription().generateIv());
                fos.close();


                //getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("homeDir", "/storage").commit();
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
