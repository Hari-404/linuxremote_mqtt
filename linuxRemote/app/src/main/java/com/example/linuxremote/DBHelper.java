package com.example.linuxremote;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "machineData";
    private static final String TABLE_NAME = "ipaddress";
    private static final String SNO = "sno";
    private static final String MNAME = "machine_name";
    private static final String IP = "ipaddress";
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + SNO + " INTEGER PRIMARY KEY," + MNAME + " TEXT," + IP + " TEXT"
            + ")";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertNote(int sno, String name, String ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SNO, sno);
        values.put(MNAME, name);
        values.put(IP, ip);

        db.insert(TABLE_NAME, null, values);
        db.close();

    }

    public Cursor getNote() {
        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.query(TABLE_NAME+" WHERE "+ ROLLNO + " = "+ roll, null, null, null, null, null, null);
        //Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        //String countQuery = "SELECT  * FROM " + TABLE_NAME;
        //Cursor cursor = db.rawQuery(countQuery, null);

        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

       /* if(cur.getCount() != 0){
            cur.moveToFirst();

            do{
                String row_values = "";

                for(int i = 0 ; i < cur.getColumnCount(); i++){
                    row_values = row_values + " || " + cur.getString(i);
                }

                Log.i("db", row_values);

            }while (cur.moveToNext());
        }*/


        return cur;
    }

    public void dropTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_NAME, null,null);
    }

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int updateNote(int sno, String name, String ip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MNAME, name);
        values.put(IP, ip);
        return db.update(TABLE_NAME, values, SNO + " = ?",
                new String[]{String.valueOf(sno)});
    }

    public void deleteNote(int sno) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.i("Db", "deleteNote: "+sno);
        db.delete(TABLE_NAME, SNO+ " = " + sno, null);
        db.close();
    }
}

