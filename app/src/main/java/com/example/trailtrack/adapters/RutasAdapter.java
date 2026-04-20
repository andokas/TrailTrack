package com.example.trailtrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trailtrack.R;
import com.example.trailtrack.models.Ruta;

import java.util.List;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutaViewHolder> {

    private Context context;
    private List<Ruta> rutas;
    private OnRutaClickListener listener;

    public interface OnRutaClickListener {
        void onRutaClick(Ruta ruta);
    }

    public RutasAdapter(Context context, List<Ruta> rutas, OnRutaClickListener listener) {
        this.context = context;
        this.rutas = rutas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_ruta, parent, false);
        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta ruta = rutas.get(position);

        holder.tvNombre.setText(ruta.getNombre());
        holder.tvDistancia.setText(String.format("%.2f km", ruta.getDistancia()));

        // Formatear duración
        int duracion = ruta.getDuracion();
        int horas = duracion / 3600;
        int minutos = (duracion % 3600) / 60;
        int segundos = duracion % 60;
        holder.tvDuracion.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));

        holder.tvFecha.setText(ruta.getFecha());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRutaClick(ruta);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return rutas != null ? rutas.size() : 0;
    }

    public static class RutaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDistancia, tvDuracion, tvFecha;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreRuta);
            tvDistancia = itemView.findViewById(R.id.tvDistanciaRuta);
            tvDuracion = itemView.findViewById(R.id.tvDuracionRuta);
            tvFecha = itemView.findViewById(R.id.tvFechaRuta);
        }
    }
}