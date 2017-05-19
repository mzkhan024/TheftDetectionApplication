package com.microtree.www.theftdetectionapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by MZKhan on 5/12/2017.
 */

public class Database extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "TheftAppDB";
    public static final int VERSION = 1;

    public static final String TABLE_NAME = "users";

    public static final String DROP_ITEM= "DROP TABLE IF EXISTS "+ TABLE_NAME;
    public static final String READ_ITEM= "SELECT * FROM "+TABLE_NAME+"";


    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String PASSWORD = "password";




    public static final String CREATE_TABLE= "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +""+
            "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
            USERNAME +" TEXT not null, "+
            EMAIL +" TEXT not null, "+
            PHONE +" TEXT not null, "+
            PASSWORD +" INTEGER not null)";

    String TAG = Database.class.getSimpleName();


    public Database(Context contex) {
        super(contex, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
            Log.d(TAG, "CREATED");

        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(DROP_ITEM);
        Log.d(TAG, "UPGRADED");
        this.onCreate(db);

    }

    public boolean Register(String user, String email, String phone, String password){
        boolean success = false;
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USERNAME, user);
        cv.put(EMAIL, email);
        cv.put(PHONE, phone);
        cv.put(PASSWORD, password);

        try {
            long insert = database.insert(TABLE_NAME, null, cv);
            success = insert != -1;
            database.close();

        } catch (SQLException ex) {
            ex.getMessage();
        }
        return success;

    }

    public boolean Login(String email, String pass){
        boolean success = false;
        SQLiteDatabase database = this.getWritableDatabase();
        String Query = "SELECT * FROM "+TABLE_NAME+" WHERE "+EMAIL+ " = '"+email+"' AND "+PASSWORD+" = '"+pass+"'";
        Cursor cursor = database.rawQuery(Query, null);
        if (cursor.getCount()>0){
            success = true;
        }else {
            success = false;
        }

        return success;
    }


}
