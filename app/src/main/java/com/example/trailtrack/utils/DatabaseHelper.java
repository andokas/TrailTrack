package com.example.trailtrack.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "trailtrack_local.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_RUTAS = "rutas";
    public static final String COL_ID = "_id";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_DISTANCIA = "distancia";
    public static final String COL_DURACION = "duracion";
    public static final String COL_FECHA = "fecha";

    public static final String CREATE_TABLE_RUTAS =
            "CREATE TABLE " + TABLE_RUTAS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOMBRE + " TEXT, " +
                    COL_DISTANCIA + " REAL, " +
                    COL_DURACION + " INTEGER, " +
                    COL_FECHA + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RUTAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUTAS);
        onCreate(db);
    }
}