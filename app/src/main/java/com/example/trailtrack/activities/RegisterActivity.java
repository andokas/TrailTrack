package com.example.trailtrack.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trailtrack.R;
import com.example.trailtrack.network.ApiService;
import com.example.trailtrack.network.RetrofitClient;
import com.example.trailtrack.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNombre, etEmail, etPassword;
    private Button btnRegistrar, btnFoto;
    private ImageView ivFotoPerfil;
    private TextView tvLogin;
    private SessionManager sessionManager;
    private ApiService apiService;
    private Bitmap fotoBitmap = null;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnFoto = findViewById(R.id.btnFoto);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        tvLogin = findViewById(R.id.tvLogin);

        // Configurar cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            fotoBitmap = (Bitmap) result.getData().getExtras().get("data");
                            ivFotoPerfil.setImageBitmap(fotoBitmap);
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            fotoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            ivFotoPerfil.setImageBitmap(fotoBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        btnFoto.setOnClickListener(v -> {
            String[] opciones = {"Cámara", "Galería"};

            new AlertDialog.Builder(this)
                    .setTitle("Seleccionar imagen")
                    .setItems(opciones, (dialog, which) -> {
                        if (which == 0) {
                            // Cámara
                            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
                            } else {
                                abrirCamara();
                            }
                        } else {
                            // Galería
                            abrirGaleria();
                        }
                    })
                    .show();
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrar();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registrar() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);

        Map<String, String> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("email", email);
        datos.put("password", password);

        apiService.registro(datos).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();

                    int id = Integer.parseInt(String.valueOf(body.get("id")));

                    // Si hay foto, subirla
                    if (fotoBitmap != null) {
                        subirFotoPerfil(id, nombre, email, null);
                    } else {
                        sessionManager.guardarSesion(id, nombre, email, null);
                        irAMainActivity();
                    }
                } else {
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Error al registrar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnRegistrar.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirFotoPerfil(int userId, String nombre, String email, String fotoUrl) {
        try {
            // Convertir bitmap a archivo
            File fotoFile = new File(getCacheDir(), "foto_perfil.jpg");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fotoBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            FileOutputStream fos = new FileOutputStream(fotoFile);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();

            RequestBody usuarioIdBody = RequestBody.create(
                    MediaType.parse("text/plain"), String.valueOf(userId));
            RequestBody tipoBody = RequestBody.create(
                    MediaType.parse("text/plain"), "perfil");
            RequestBody fileBody = RequestBody.create(
                    MediaType.parse("image/jpeg"), fotoFile);
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData(
                    "foto", fotoFile.getName(), fileBody);

            apiService.subirFoto(usuarioIdBody, tipoBody, fotoPart)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call,
                                               Response<Map<String, Object>> response) {
                            String urlFoto = null;
                            if (response.isSuccessful() && response.body() != null) {
                                urlFoto = (String) response.body().get("url");
                            }
                            sessionManager.guardarSesion(userId, nombre, email, urlFoto);
                            irAMainActivity();
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            sessionManager.guardarSesion(userId, nombre, email, null);
                            irAMainActivity();
                        }
                    });

        } catch (IOException e) {
            sessionManager.guardarSesion(userId, nombre, email, null);
            irAMainActivity();
        }
    }

    private void irAMainActivity() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }
}