package com.example.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextClassName, editTextQuantity;
    private Button buttonClearAll, buttonUpdate;
    private RecyclerView recyclerViewItems;
    private ArrayList<String> itemsList;
    private ItemAdapter adapter;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextClassName = findViewById(R.id.editTextClassName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        buttonClearAll = findViewById(R.id.buttonClearAll);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        fabSave = findViewById(R.id.fabSave);

        // Initialize RecyclerView
        itemsList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);
        adapter = new ItemAdapter(itemsList, dbHelper);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(adapter);

        // Load items from database
        loadItemsFromDatabase();

        // Save Button Listener
        fabSave.setOnClickListener(v -> saveItemToDatabase());

        // Clear All Button Listener
        buttonClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(" مسح الكل")
                    .setMessage("أنت متاكد يا تيحه أنك عايز تمسح كل حاجه ولا انت مصطبح")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.clearAllItems();
                        itemsList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "شغلانه خره مسحتلك ياعم كل حاجه", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Update Button Listener
        buttonUpdate.setOnClickListener(v -> {
            int selectedItemPosition = adapter.getSelectedItemPosition();

            if (selectedItemPosition != RecyclerView.NO_POSITION) {
                String selectedItem = itemsList.get(selectedItemPosition);
                String[] parts = selectedItem.split(" - ");

                if (parts.length >= 3) {
                    int itemId = Integer.parseInt(parts[0]);
                    String currentClassName = parts[1];
                    int currentQuantity = Integer.parseInt(parts[2]);

                    // Create update dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.update_dialog, null);

                    EditText editClassName = dialogView.findViewById(R.id.editTextUpdateClassName);
                    EditText editQuantity = dialogView.findViewById(R.id.editTextUpdateQuantity);
                    Button btnUpdateDialog = dialogView.findViewById(R.id.buttonUpdateDialog);

                    editClassName.setText(currentClassName);
                    editQuantity.setText(String.valueOf(currentQuantity));

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    btnUpdateDialog.setOnClickListener(innerV -> {
                        String newClassName = editClassName.getText().toString().trim();
                        String newQuantityStr = editQuantity.getText().toString().trim();

                        if (!newClassName.isEmpty() && !newQuantityStr.isEmpty()) {
                            try {
                                int newQuantity = Integer.parseInt(newQuantityStr);
                                updateItem(newClassName, newQuantityStr, itemId);
                                dialog.dismiss();
                            } catch (NumberFormatException e) {
                                Toast.makeText(MainActivity.this, "ياعم انت بتستلوح أكتب رقم ياعم ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, " فوق كده الله يرضى عليك وأكتب كل البيانات", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show();
                } else {
                    Toast.makeText(this, "Error in stored data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Select an item to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItemToDatabase() {
        String className = editTextClassName.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();

        if (!className.isEmpty() && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                boolean isInserted = dbHelper.insertItem(className, quantityStr);

                if (isInserted) {
                    Toast.makeText(this, " صبح صبح يا تيحه", Toast.LENGTH_SHORT).show();
                    loadItemsFromDatabase();
                } else {
                    Toast.makeText(this, "فوق كده لحالك مفيش حاجه أتسجلت", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }

        editTextClassName.setText("");
        editTextQuantity.setText("");
    }

    private void loadItemsFromDatabase() {
        Cursor cursor = dbHelper.getAllItems();
        itemsList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String className = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));

                    itemsList.add(id + " - " + className + " - " + quantity);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: Missing column in database");
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void updateItem(String className, String quantity, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLASS_NAME, className);
        values.put(DatabaseHelper.COLUMN_QUANTITY, Integer.parseInt(quantity));

        long result = db.update(DatabaseHelper.TABLE_ITEMS, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (result != 0) {
            Toast.makeText(this, "حدثنا البيانات يا معاليك", Toast.LENGTH_SHORT).show();
            loadItemsFromDatabase();
        } else {
            Toast.makeText(this, " متحدثش والله شوف كده انت نيلت ايه غلط", Toast.LENGTH_SHORT).show();
        }
    }
}