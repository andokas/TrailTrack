package com.example.trailtrack.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trailtrack.R;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    private EditText etNuevoNombre, etNuevaPassword, etConfirmarPassword;
    private Button btnGuardarCambios, btnReiniciarStats;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        sessionManager = new SessionManager(this);

        etNuevoNombre = findViewById(R.id.etNuevoNombre);
        etNuevaPassword = findViewById(R.id.etNuevaPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnReiniciarStats = findViewById(R.id.btnReiniciarStats);

        // Mostrar nombre actual como hint
        etNuevoNombre.setHint(sessionManager.getNombre());

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios();
            }
        });

        btnReiniciarStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarReiniciarStats();
            }
        });
    }

    private void guardarCambios() {
        String nuevoNombre = etNuevoNombre.getText().toString().trim();
        String nuevaPassword = etNuevaPassword.getText().toString().trim();
        String confirmarPassword = etConfirmarPassword.getText().toString().trim();

        // Validar que haya algo que cambiar
        if (nuevoNombre.isEmpty() && nuevaPassword.isEmpty()) {
            Toast.makeText(this, "No hay cambios que guardar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar contraseñas
        if (!nuevaPassword.isEmpty() && !nuevaPassword.equals(confirmarPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardarCambios.setEnabled(false);

        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario_id", sessionManager.getId());

        if (!nuevoNombre.isEmpty()) {
            datos.put("nombre", nuevoNombre);
        }
        if (!nuevaPassword.isEmpty()) {
            datos.put("password", nuevaPassword);
        }

        RetrofitClient.getApiService().editarPerfil(datos)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        btnGuardarCambios.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            // Actualizar sesión con el nuevo nombre si cambió
                            if (!nuevoNombre.isEmpty()) {
                                sessionManager.guardarSesion(
                                        sessionManager.getId(),
                                        nuevoNombre,
                                        sessionManager.getEmail(),
                                        sessionManager.getFoto());
                            }
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Perfil actualizado correctamente",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Error al actualizar perfil",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        btnGuardarCambios.setEnabled(true);
                        Toast.makeText(EditarPerfilActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmarReiniciarStats() {
        new AlertDialog.Builder(this)
                .setTitle("Reiniciar estadísticas")
                .setMessage("¿Estás seguro? Se eliminarán todas tus rutas guardadas.")
                .setPositiveButton("Sí, reiniciar", (dialog, which) -> reiniciarStats())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void reiniciarStats() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("usuario_id", sessionManager.getId());
        datos.put("reiniciar_stats", true);

        RetrofitClient.getApiService().editarPerfil(datos)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call,
                                           Response<Map<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditarPerfilActivity.this,
                                    "Estadísticas reiniciadas",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(EditarPerfilActivity.this,
                                "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}