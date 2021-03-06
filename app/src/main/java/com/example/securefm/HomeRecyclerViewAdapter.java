package com.example.securefm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ListViewHolder> {

    private List<String> directoryEntries;
    private File currentDirectory;
    private HomeFragment homeFragment;

    HomeRecyclerViewAdapter(List<String> directoryEntries, File currentDirectory, HomeFragment context){
        this.directoryEntries = directoryEntries;
        this.currentDirectory = currentDirectory;
        this.homeFragment = context;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_item, parent, false);

        return new ListViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.listItemNumView.setText(new File(directoryEntries.get(position)).getName());
        if (directoryEntries.get(position).equals("..")) {
            holder.imageView.setImageResource(R.drawable.ic_folder_open_24dp);
        } else if (new File(directoryEntries.get(position)).isFile()){
            holder.imageView.setImageResource(R.drawable.ic_insert_drive_file_24dp);
        } else if (new File(directoryEntries.get(position)).isDirectory()){
            holder.imageView.setImageResource(R.drawable.ic_folder_24dp);
        }
    }

    @Override
    public int getItemCount() {
        return directoryEntries.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        TextView listItemNumView;
        ImageView imageView;

        public ListViewHolder(final View itemView) {
            super(itemView);
            listItemNumView = itemView.findViewById(R.id.list);
            imageView = itemView.findViewById(R.id.image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    homeFragment.openDialog(new File(directoryEntries.get(getAdapterPosition())).getAbsoluteFile());
                    return true;
                }
            });
        }
    }
}
