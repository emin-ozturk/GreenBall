package com.emin.greenball;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_NAME = "game";
    private static final String ID = "id";
    private static final String BEST_SCORE = "skor";
    private static final String MUSIC = "music"; //0: ses kapalı, 1: ses açık
    private static final String VIBRATION = "vibration"; //0: titreşim kapalı, 1: titreşim açık
    private static final String LANGUAGE = "language"; //0: türkçe, 1: ingilizce


    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BEST_SCORE + " TEXT NOT NULL, " +
                MUSIC + " TEXT NOT NULL, " +
                VIBRATION + " TEXT NOT NULL, " +
                LANGUAGE + " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void createTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BEST_SCORE, 0);
        cv.put(MUSIC, 1);
        cv.put(VIBRATION, 1);
        cv.put(LANGUAGE, 0);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public List<String> list() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> data = new ArrayList<>();
        try {
            String[] row = {BEST_SCORE, MUSIC, VIBRATION, LANGUAGE};
            Cursor cursor = db.query(TABLE_NAME, row, null, null, null, null, null);
            while (cursor.moveToNext()) {
                data.add(cursor.getString(0)
                        +
                        " _ "
                        + cursor.getString(1)
                        +
                        " _ "
                        + cursor.getString(2)
                        +
                        " _ "
                        + cursor.getString(3));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return data;
    }

    public int getBestScore() {
        int bestScore = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] row = {BEST_SCORE};
            Cursor cursor = db.query(TABLE_NAME, row, null, null, null, null, null);
            while (cursor.moveToNext()) {
                bestScore = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestScore;
    }

    public int getMusic() {
        int music = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] row = {MUSIC};
            Cursor cursor = db.query(TABLE_NAME, row, null, null, null, null, null);
            while (cursor.moveToNext()) {
                music = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return music;
    }

    public int getVibration() {
        int vibration = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] row = {VIBRATION};
            Cursor cursor = db.query(TABLE_NAME, row, null, null, null, null, null);
            while (cursor.moveToNext()) {
                vibration = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vibration;
    }

    public int getLanguage() {
        int language = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String[] row = {LANGUAGE};
            Cursor cursor = db.query(TABLE_NAME, row, null, null, null, null, null);
            while (cursor.moveToNext()) {
                language = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return language;
    }

    public void updateBestScore(int bestScore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BEST_SCORE, bestScore);
        db.update(TABLE_NAME, cv, null, null);
        db.close();
    }

    public void updateMusic(int music) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MUSIC, music);
        db.update(TABLE_NAME, cv, null, null);
        db.close();
    }

    public void updateVibration(int vibration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(VIBRATION, vibration);
        db.update(TABLE_NAME, cv, null, null);
        db.close();
    }

    public void updateLanguage(int language) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LANGUAGE, language);
        db.update(TABLE_NAME, cv, null, null);
        db.close();
    }
}
