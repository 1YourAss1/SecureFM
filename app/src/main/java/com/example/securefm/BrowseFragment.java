package com.example.securefm;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
                                new EncryptTask().execute(file, new Encryption(getContext()));
                                /*Encryption encription = new Encryption(getContext());
                                File[] files = {
                                        new File("/storage/emulated/0/test/test1.jpg"),
                                        new File("/storage/emulated/0/test/Nice.mp3"),
                                        new File("/storage/emulated/0/test/Nice.mp4"),
                                        new File("/storage/emulated/0/test/test.bmp"),
                                        new File("/storage/emulated/0/test/test.gif"),
                                        new File("/storage/emulated/0/test/test2.jpg"),
                                        new File("/storage/emulated/0/test/test20.pdf"),
                                        new File("/storage/emulated/0/test/test34.djvu"),
                                        new File("/storage/emulated/0/test/test.pdf"),
                                        new File("/storage/emulated/0/test/test89.pdf")
                                };
                                String[] modes = {
                                        "ECB",
                                        "CTR",
                                        "CBC",
                                        "OFB",
                                        "CFB"
                                };
                                for (String mode: modes) {
                                    Log.i("Encryption mode", mode);
                                    for (File filek: files) {
                                        long start = System.currentTimeMillis();
                                        encription.encryptFile(filek, password, mode);
                                        long stop = System.currentTimeMillis();
                                        double time = Double.valueOf(stop - start)/1000;
                                        Log.i("Encryption time", filek.getName() + " (" + filek.length() + " б) - " + time + " с");
                                    }
                                }*/
                            } catch (Exception ex) {
                                Log.e("AsynTask Error", ex.getMessage());
                            }
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
            Encryption encryption = (Encryption)args[1];
            long start = System.currentTimeMillis();
            encryption.encryptFile(file, password);
            long stop = System.currentTimeMillis();
            double time = Double.valueOf(stop - start)/1000;
            Object[] arr = new Object[2];
            arr[0] = time;
            arr[1] = file;
            return arr;
        }

        @Override
        protected void onPostExecute(Object[] result) {
            Double time = (Double) result[0];
            File file = (File) result[1];
            String bigText = "Файл " + file.getName() + " (" + file.length() +" б) зашифрован за " + time + " с";
            Toast.makeText(getContext(),
                    bigText,
                    Toast.LENGTH_SHORT).show();
            Log.i("Encryption time", file.getName() + " (" + file.length() + " б) - " + time + " с");
        }
    }

    /*class EncryptTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Шифрование началось", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Object... args) {
            File file = (File)args[0];
            Encryption encription = (Encryption)args[1];
            encription.encryptFile(file, password);
            return null;
        }

    }*/

}
