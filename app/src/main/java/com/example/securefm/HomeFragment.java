package com.example.securefm;

import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView homeRecyclerView;
    private HomeRecyclerViewAdapter recyclerAdapter;
    private TextView textDir;
    private String password;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<String> directoryEntries = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylcer_view, container, false);
        Bundle bundle = this.getArguments();
        homeRecyclerView = view.findViewById(R.id.rv);
        textDir = view.findViewById(R.id.textDir);
        password = bundle.getString("PASSWORD");

        mSwipeRefreshLayout = view.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fill(new File(getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage")));
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        homeRecyclerView.setLayoutManager(layoutManager);

        textDir.setText(getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage"));
        fill(new File(getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage")));

        return view;
    }

    public void fill(File homeDir) {
        File[] files = homeDir.listFiles();
        this.directoryEntries.clear();

        for (File file : files) {
            this.directoryEntries.add(file.getAbsolutePath());
        }

        recyclerAdapter = new HomeRecyclerViewAdapter(directoryEntries, new File(getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage")), this);
        homeRecyclerView.setAdapter(recyclerAdapter);
    }

    //Диалог выбора действия над файлом
    public void openDialog(final File file) {
        final String arr[] = {"Раcшифровать", "Удалить"};
        new AlertDialog.Builder(getActivity())
                .setTitle("Выберите действие")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            new DecryptTask().execute(file, new Encription(getContext()));
                        } else if (which == 1){
                            file.delete();
                            fill(new File(getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE).getString("homeDir", "/storage")));
                            Toast.makeText(getActivity(), "Удалено успешно", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .show();
    }

    class DecryptTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Расшифрование началось", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Object... args) {
            File file = (File)args[0];
            Encription encription = (Encription)args[1];
            encription.decryptFile(file, password);
            return null;
        }

    }
}
