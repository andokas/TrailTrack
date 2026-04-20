package com.example.trailtrack.network;

import com.example.trailtrack.models.Ruta;
import com.example.trailtrack.models.Usuario;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login.php")
    Call<Map<String, Object>> login(@Body Map<String, String> datos);

    @POST("registro.php")
    Call<Map<String, Object>> registro(@Body Map<String, String> datos);

    @POST("editar_perfil.php")
    Call<Map<String, Object>> editarPerfil(@Body Map<String, Object> datos);

    @POST("guardar_ruta.php")
    Call<Map<String, Object>> guardarRuta(@Body Ruta ruta);

    @GET("mis_rutas.php")
    Call<Map<String, Object>> getMisRutas(@Query("usuario_id") int usuarioId);

    @Multipart
    @POST("subir_foto.php")
    Call<Map<String, Object>> subirFoto(
            @Part("usuario_id") RequestBody usuarioId,
            @Part("tipo") RequestBody tipo,
            @Part MultipartBody.Part foto
    );

    @POST("actualizar_token.php")
    Call<Map<String, Object>> actualizarToken(@Body Map<String, String> datos);
}