package com.programmer.zombie.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/*
 *
 * Created by Sami Youssef on 3/31/2016.
 */
public class FavouriteMoviesDataBase extends SQLiteOpenHelper {

    private static final String DB_NAME = "FavouriteMovies"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    public FavouriteMoviesDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Favourite ("
                + "_id INTEGER , "
                + "rate TEXT, "
                + "reliseDate TEXT, "
                + "overview TEXT, "
                + "posterPath TEXT, "
                + "title TEXT);");
    }

    public static void insertData(SQLiteDatabase db, int id, String rate, String reliseDate, String overview, String posterPath, String title) {
        ContentValues data = new ContentValues();
        data.put("_id", id);
        data.put("rate", rate);
        data.put("reliseDate", reliseDate);
        data.put("overview", overview);
        data.put("posterPath", posterPath);
        data.put("title", title);

        db.insert("Favourite", null, data);
    }

    public static void deleteData(SQLiteDatabase db, int id) {
        db.delete("Favourite", "_id = " + id, null);
    }

    public static boolean isFavourite(Context context, int id) {
        try {
            SQLiteOpenHelper starbuzzDatabaseHelper = new FavouriteMoviesDataBase(context);
            SQLiteDatabase db = starbuzzDatabaseHelper.getReadableDatabase();
            Cursor cursor = db.query("Favourite",
                    new String[]{"title"},
                    "_id = ?",
                    new String[]{Integer.toString(id)},
                    null, null, null);

            if (cursor.getCount() > 0) {
                cursor.close();
                db.close();
                return true;
            }
        } catch (SQLiteException e) {
            return false;
        }
        return false;
    }

    public static Cursor LoadFavouriteMovies(Context context) {
        Cursor cursor = null;
        try {
            SQLiteOpenHelper movieDatabaseHelper = new FavouriteMoviesDataBase(context);
            SQLiteDatabase db = movieDatabaseHelper.getReadableDatabase();

            String selectQuery = "SELECT  * FROM Favourite";
            cursor = db.rawQuery(selectQuery, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return cursor;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
