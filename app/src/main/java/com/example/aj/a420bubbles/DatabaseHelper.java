package com.example.aj.a420bubbles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// this was created with the help of http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
public class DatabaseHelper extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "420BubblesDB";

    // Contacts table name
    private static final String TABLE_SCORES = "scores";

    // Contacts Table Columns names
    private static final String KEY_ID = "ID";
    private static final String KEY_PLAYER_NAME = "playerName";
    private static final String KEY_SCORE = "score";

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_PLAYER_NAME + " TEXT,"
                + KEY_SCORE + " INTEGER" + ")";

        db.execSQL(CREATE_SCORES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void truncateTableScores(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_SCORES);
    }

    // Adding new Score
    void addScore(Score score) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_PLAYER_NAME, score.getPlayerName()); // Contact Name
        contentValues.put(KEY_SCORE, score.getScore()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_SCORES, null, contentValues);
        db.close(); // Closing database connection
    }

    // Getting All Scores
    public List<Score> getAllScores() {

        List<Score> scoreList = new ArrayList<Score>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SCORES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            do {

                Score score = new Score();

                score.setID(Integer.parseInt(cursor.getString(0)));
                score.setPlayerName(cursor.getString(1));
                score.setScore(Integer.parseInt(cursor.getString(2)));

                // Adding score to scoreList
                scoreList.add(score);
            } while (cursor.moveToNext());
        }

        // return scoreList
        return scoreList;
    }
}
