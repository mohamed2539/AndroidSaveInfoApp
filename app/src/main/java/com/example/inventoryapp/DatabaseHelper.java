package com.example.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Constants for Database and Table
    private static final String DATABASE_NAME = "InventoryApp.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CLASS_NAME = "class_name";
    public static final String COLUMN_QUANTITY = "quantity";

    // SQL Statement to Create Table
    private static final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CLASS_NAME + " TEXT, " +
            COLUMN_QUANTITY + " INTEGER);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    /**
     * Method to Insert an Item into the Database
     */
    public boolean insertItem(String className, String quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, className);

        try {
            values.put(COLUMN_QUANTITY, Integer.parseInt(quantity)); // Convert quantity to integer
            long result = db.insert(TABLE_ITEMS, null, values);
            return result != -1; // Return true if insertion was successful
        } catch (NumberFormatException e) {
            return false; // Return false if quantity is not a valid number
        }
    }

    /**
     * Method to Get All Items from the Database
     */
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_ITEMS,
                new String[]{COLUMN_ID, COLUMN_CLASS_NAME, COLUMN_QUANTITY},
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Method to Delete an Item from the Database
     */
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ITEMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return result > 0; // Return true if deletion was successful
    }

    /**
     * Method to Clear All Items from the Database
     */
    public void clearAllItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, null, null); // Delete all rows
    }
}