package com.example.securefm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RadioGroup radioGroupAlgo, radioGroupMode;
    private RadioButton radioButtonGOST28147, radioButtonGOST3412_2015, radioButtonECB, radioButtonCTR, radioButtonCBC, radioButtonOFB, radioButtonCFB;
    private String algorithm, mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Настройки");

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        //при возвращении ошибка
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        algorithm = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("algorithm", "GOST-28147");
        radioButtonGOST28147 = findViewById(R.id.radioButtonGOST28147);
        radioButtonGOST3412_2015 = findViewById(R.id.radioButtonGOST3412_2015);
        if (algorithm.equals("GOST-28147")) {
            radioButtonGOST28147.setChecked(true);
        } else if (algorithm.equals("GOST3412-2015")){
            radioButtonGOST3412_2015.setChecked(true);
        }

        radioGroupAlgo = findViewById(R.id.radioGroupAlgo);
        radioGroupAlgo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.radioButtonGOST28147:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("algorithm", "GOST-28147").commit();
                        Toast.makeText(getApplicationContext(), "Выбран ГОСТ 28147-89", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.radioButtonGOST3412_2015:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("algorithm", "GOST3412-2015").commit();
                        Toast.makeText(getApplicationContext(), "Выбран ГОСТ Р 34.12-2015", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });

        mode = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("mode", "ECB");
        radioButtonECB = findViewById(R.id.radioButtonECB);
        radioButtonCTR = findViewById(R.id.radioButtonCTR);
        radioButtonCBC = findViewById(R.id.radioButtonCBC);
        radioButtonOFB = findViewById(R.id.radioButtonOFB);
        radioButtonCFB = findViewById(R.id.radioButtonCFB);
        if (mode.equals("ECB")) {
            radioButtonECB.setChecked(true);
        } else if (mode.equals("CTR")) {
            radioButtonCTR.setChecked(true);
        } else if (mode.equals("CBC")) {
            radioButtonCBC.setChecked(true);
        } else if (mode.equals("OFB")) {
            radioButtonOFB.setChecked(true);
        } else if (mode.equals("CFB")) {
            radioButtonCFB.setChecked(true);
        }

        radioGroupMode = findViewById(R.id.radioGroupMode);
        radioGroupMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.radioButtonECB:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("mode", "ECB").commit();
                        break;
                    case R.id.radioButtonCTR:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("mode", "CTR").commit();
                        break;
                    case R.id.radioButtonCBC:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("mode", "CBC").commit();
                        break;
                    case R.id.radioButtonOFB:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("mode", "OFB").commit();
                        break;
                    case R.id.radioButtonCFB:
                        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("mode", "CFB").commit();
                        break;
                    default:
                        break;
                }
            }
        });

    }
}

