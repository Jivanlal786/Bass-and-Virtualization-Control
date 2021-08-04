package com.example.mymusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MySongHolder> {

    ArrayList<modelSong> songlist;
    Context context;
    OnClickListener onClickListener;

    public ListAdapter(ArrayList<modelSong> songlist, Context context, OnClickListener onClickListener) {
        this.songlist = songlist;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public MySongHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_layout,parent,false);
        return new MySongHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  ListAdapter.MySongHolder holder, int position) {

        holder.tvTitle.setText(songlist.get(position).getTitle());
        holder.tvArtist.setText(songlist.get(position).getArtist());

        holder.ivAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.itemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songlist.size();
    }

    public class MySongHolder extends RecyclerView.ViewHolder {
        
        ImageView ivAlbum;
        TextView tvTitle,tvArtist;
        
        public MySongHolder(@NonNull  View itemView) {
            super(itemView);
            
            ivAlbum = itemView.findViewById(R.id.iv_album);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
        }
    }
}
