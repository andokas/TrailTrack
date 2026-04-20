package com.example.trailtrack.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.trailtrack.R;
import com.example.trailtrack.activities.MainActivity;
import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrailWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void actualizarWidget(Context context, AppWidgetManager appWidgetManager,
                                  int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.widget_trail);

        // Click en el widget abre la app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRutas, pendingIntent);

        SessionManager sessionManager = new SessionManager(context);

        if (!sessionManager.isLoggedIn()) {
            views.setTextViewText(R.id.widgetRutas, "Inicia sesión");
            views.setTextViewText(R.id.widgetKm, "");
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        // Cargar estadísticas del servidor
        RetrofitClient.getApiService()
                .getMisRutas(sessionManager.getId())
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Gson gson = new Gson();
                            Type tipoLista = new TypeToken<List<Ruta>>() {}.getType();
                            String json = gson.toJson(response.body().get("rutas"));
                            List<Ruta> rutas = gson.fromJson(json, tipoLista);

                            int totalRutas = rutas.size();
                            float totalKm = 0f;
                            for (Ruta ruta : rutas) {
                                totalKm += ruta.getDistancia();
                            }

                            views.setTextViewText(R.id.widgetRutas,
                                    totalRutas + " rutas");
                            views.setTextViewText(R.id.widgetKm,
                                    String.format("%.2f km", totalKm));
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        views.setTextViewText(R.id.widgetRutas, "Sin conexión");
                        views.setTextViewText(R.id.widgetKm, "");
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                });
    }
}