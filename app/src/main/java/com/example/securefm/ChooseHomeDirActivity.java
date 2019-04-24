package com.example.securefm;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChooseHomeDirActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView textDir;
    private File currentDirectory;
    private List<String> directoryEntries = new ArrayList<>();
    private ChooseHomeDirRecyclerViewAdapter recyclerAdapter;
    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_home_dir);
        setTitle("Выбор домашней папки");

        mSwipeRefreshLayout = findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                browseTo(currentDirectory);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv);
        textDir = findViewById(R.id.textDir);

        int permissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStatus == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS_STORAGE,
                    REQUEST_CODE_EXTERNAL_STORAGE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //currentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        currentDirectory = new File(getSharedPreferences("PREFERENCE", MODE_PRIVATE).getString("homeDir", "/storage"));
        textDir.setText(currentDirectory.getPath());
        browseTo(currentDirectory);
        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText dirName = new EditText(view.getContext());
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Новая папака")
                        .setMessage("Введите название папки")
                        .setView(dirName)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    File dir = new File(currentDirectory.getAbsolutePath() + "/" + dirName.getText().toString());
                                    dir.mkdirs();
                                    browseTo(currentDirectory);
                                } catch (Exception ex) {
                                    Log.e("Make Directory", ex.getMessage());
                                    Toast.makeText(ChooseHomeDirActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }

    public void setDirTitle(String title) {
        textDir.setText(title);
    }

    //Отображение содержимого директории
    public void browseTo(final File aDirectory) {
        this.currentDirectory = aDirectory;
        if (aDirectory.getAbsolutePath().equals("/storage") || aDirectory.getAbsolutePath().equals("/storage/emulated")) {
            firstFill();
            this.currentDirectory = new File("/storage");
        } else {
            fill(aDirectory.listFiles());
        }
        recyclerAdapter = new ChooseHomeDirRecyclerViewAdapter(directoryEntries, currentDirectory, this);
        recyclerView.setAdapter(recyclerAdapter);
        setDirTitle(currentDirectory.getAbsolutePath());
    }


    //Заполнение списка файлов текущей директории
    private void fill(File[] files) {
        this.directoryEntries.clear();
        this.directoryEntries.add("..");

        for (File file : files) {
            if (file.isDirectory()) {
                this.directoryEntries.add(file.getAbsolutePath());
            }
        }
    }


    private void firstFill() {
        this.directoryEntries.clear();
        File[] externalDirs = getExternalFilesDirs(null);
        for (File file: externalDirs) {
            int ind = file.getAbsolutePath().lastIndexOf("/Android");
            String path = file.getAbsolutePath().substring(0, ind);
            directoryEntries.add(path);
        }
    }

    //Переход на уровень выше текущей директории
    public void upOneLevel() {
        if (this.currentDirectory.getParent() != null) {
            this.browseTo(this.currentDirectory.getParentFile());
        }
    }

    public void openDialog(final File file) {
        final String arr[] = {"Выбрать папку"};
        new AlertDialog.Builder(this)
                .setTitle("Выберите действие")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putString("homeDir", file.getAbsolutePath()).commit();
                            Toast.makeText(ChooseHomeDirActivity.this, file.getAbsolutePath() +
                                    " выбрана в качестве домашней директории", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ChooseHomeDirActivity.this, MainActivity.class));
                        }
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        if (this.currentDirectory.getAbsolutePath().equals("/storage")){
            ChooseHomeDirActivity.super.onBackPressed();
        } else {
            upOneLevel();
        }
    }
}
