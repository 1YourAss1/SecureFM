package com.example.securefm;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class BrowseFragment extends Fragment {
    private RecyclerView recyclerView;
    private String password;
    private TextView textDir;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private File currentDirectory = new File("/storage");
    private BrowseRecyclerViewAdapter recyclerAdapter;
    private List<String> directoryEntries = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Bundle bundle = this.getArguments();
        password = bundle.getString("PASSWORD");
        View view = inflater.inflate(R.layout.recylcer_view, container, false);
        recyclerView = view.findViewById(R.id.rv);
        textDir = view.findViewById(R.id.textDir);
        mSwipeRefreshLayout = view.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                browseTo(currentDirectory);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        textDir.setText(currentDirectory.getPath());
        browseTo(new File("/storage"));

        return view;
    }

    public void setTitle(String title) {
        textDir.setText(title);
    }

    //Переход на уровень выше
    public void upOneLevel() {
        if (this.currentDirectory.getParent() != null) {
            this.browseTo(this.currentDirectory.getParentFile());
        }
    }

    public void browseTo(final File aDirectory) {
        if (aDirectory.isDirectory()) {
            this.currentDirectory = aDirectory;
            if (aDirectory.getAbsolutePath().equals("/storage") || aDirectory.getAbsolutePath().equals("/storage/emulated")) {
                firstFill();
                this.currentDirectory = new File("/storage");
            } else {
                fill(aDirectory.listFiles());
            }
            recyclerAdapter = new BrowseRecyclerViewAdapter(directoryEntries, currentDirectory, this);
            recyclerView.setAdapter(recyclerAdapter);
            setTitle(currentDirectory.getAbsolutePath());
        } else {
            try {
                //Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("file://" + aDirectory.getAbsolutePath()));
                //Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.fromFile(aDirectory));
                //startActivity(i);
                Toast.makeText(getActivity(), "Открывать мока можно только на <7 Android", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Log.e("Intent Error", ex.getMessage());
            }
        }
    }

    //Заполнение списка файлов в текущей дериктории
    private void fill(File[] files) {
        this.directoryEntries.clear();
        this.directoryEntries.add("..");

        for (File file : files) {
            this.directoryEntries.add(file.getAbsolutePath());
        }
    }

    private void firstFill() {
        this.directoryEntries.clear();
        File[] externalDirs = getActivity().getExternalFilesDirs(null);
        for (File file: externalDirs) {
            int ind = file.getAbsolutePath().lastIndexOf("/Android");
            String path = file.getAbsolutePath().substring(0, ind);
            directoryEntries.add(path);
        }
    }

    //Диалог выбора действия над файлом
    public void openDialog(final File file) {
        final String arr[] = {"Зашифровать", "Удалить"};
        new AlertDialog.Builder(getActivity())
                .setTitle("Выберите действие")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            try {
                            new EncryptTask().execute(file, new Encription(getContext()));
                            } catch (Exception ex) {
                                Log.e("AsynTask Error", ex.getMessage());
                            }
                            /*long start = System.currentTimeMillis();
                            new Encription().encryptFile(
                                    file,
                                    password,
                                    getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage"),
                                    getContext());
                            long stop = System.currentTimeMillis();
                            double time = Double.valueOf(stop - start)/1000;*/
                            //browseTo(currentDirectory);
                            //Toast.makeText(getActivity(), "Файл успешно зашифрован за " + time + " с", Toast.LENGTH_SHORT).show();
                            //Log.i("Encryption Time for " + file.getName(), String.valueOf(stop - start) + " ms");
                        } else if (which == 1){
                            file.delete();
                            browseTo(currentDirectory);
                            Toast.makeText(getActivity(), "Удалено успешно", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    class EncryptTask extends AsyncTask<Object, Void, Object[]> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Шифрование началось", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Object[] doInBackground(Object... args) {
            File file = (File)args[0];
            Encription encription = (Encription)args[1];
            long start = System.currentTimeMillis();
            encription.encryptFile(file, password);
            long stop = System.currentTimeMillis();
            double time = Double.valueOf(stop - start)/1000;
            Object[] arr = new Object[2];
            arr[0] = time;
            arr[1] = file.getName();
            return arr;
        }

        @Override
        protected void onPostExecute(Object[] result) {
            Double time = (Double) result[0];
            String fileName = (String) result[1];
            NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(getContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Шифрование завершено")
                    .setContentText("Файл " + fileName + " зашифрован за " + time + " с");

            Notification notification = notificationCompat.build();

            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        }
    }

}
