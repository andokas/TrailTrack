package com.example.trailtrack.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.trailtrack.utils.DatabaseHelper;

public class RutasProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.trailtrack.providers.RutasProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/rutas");

    private static final int RUTAS = 1;
    private static final int RUTA_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "rutas", RUTAS);
        uriMatcher.addURI(AUTHORITY, "rutas/#", RUTA_ID);
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case RUTAS:
                cursor = db.query(DatabaseHelper.TABLE_RUTAS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case RUTA_ID:
                selection = DatabaseHelper.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DatabaseHelper.TABLE_RUTAS, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long id;
        switch (uriMatcher.match(uri)) {
            case RUTAS:
                id = db.insert(DatabaseHelper.TABLE_RUTAS, null, values);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas;
        switch (uriMatcher.match(uri)) {
            case RUTAS:
                filas = db.update(DatabaseHelper.TABLE_RUTAS, values,
                        selection, selectionArgs);
                break;
            case RUTA_ID:
                selection = DatabaseHelper.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                filas = db.update(DatabaseHelper.TABLE_RUTAS, values,
                        selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return filas;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int filas;
        switch (uriMatcher.match(uri)) {
            case RUTAS:
                filas = db.delete(DatabaseHelper.TABLE_RUTAS, selection, selectionArgs);
                break;
            case RUTA_ID:
                selection = DatabaseHelper.COL_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                filas = db.delete(DatabaseHelper.TABLE_RUTAS, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return filas;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case RUTAS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".rutas";
            case RUTA_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".rutas";
            default:
                throw new IllegalArgumentException("URI desconocida: " + uri);
        }
    }
}