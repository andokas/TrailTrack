package com.example.trailtrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.trailtrack.R;

import java.util.List;

public class FotosAdapter extends RecyclerView.Adapter<FotosAdapter.FotoViewHolder> {

    private Context context;
    private List<String> fotos;

    public FotosAdapter(Context context, List<String> fotos) {
        this.context = context;
        this.fotos = fotos;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        Glide.with(context)
                .load(fotos.get(position))
                .centerCrop()
                .into(holder.ivFoto);
    }

    @Override
    public int getItemCount() {
        return fotos != null ? fotos.size() : 0;
    }

    public static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFoto);
        }
    }
}