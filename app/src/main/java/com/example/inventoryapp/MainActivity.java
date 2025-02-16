package com.example.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.databinding.DataBindingUtil; // إذا كنت تستخدم Data Binding
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.inventoryapp.databinding.ActivityMainBinding; // إذا كنت تستخدم Data Binding
import com.example.inventoryapp.DatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextClassName, editTextQuantity;
    private Button buttonSave, buttonClearAll, buttonUpdate;
    private ListView listViewItems;
    private ArrayList<String> itemsList;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // إذا كنت تستخدم findViewById

// Initialize Views
        editTextClassName = findViewById(R.id.editTextClassName);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        buttonSave = findViewById(R.id.buttonSave);
        buttonClearAll = findViewById(R.id.buttonClearAll);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        // Initialize List
        itemsList = new ArrayList<>();

        // Initialize RecyclerView
        RecyclerView recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this)); // ضبط الاتجاه إلى عمودي
        ItemAdapter adapter = new ItemAdapter(this, itemsList); // إنشاء adapter
        recyclerViewItems.setAdapter(adapter); // ربط adapter بالقائمة

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);

        // Load saved data from database
        loadItemsFromDatabase();

        // Save Button Listener
        buttonSave.setOnClickListener(v -> saveItemToDatabase());

        // Update Button Listener
        buttonUpdate.setOnClickListener(v -> {
            if (listViewItems.getCheckedItemPosition() != ListView.INVALID_POSITION) {
                int position = listViewItems.getCheckedItemPosition();
                String selectedItem = itemsList.get(position);

                // استخراج ID من السجل
                String[] parts = selectedItem.split(" - ");
                if (parts.length >= 3) { // تحقق من وجود ID
                    int itemId = Integer.parseInt(parts[0]); // ID هو الجزء الأول
                    String currentClassName = parts[1];
                    int currentQuantity = Integer.parseInt(parts[2]);

                    // إنشاء حوار تعديل
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.update_dialog, null);

                    EditText editClassName = dialogView.findViewById(R.id.editTextUpdateClassName);
                    EditText editQuantity = dialogView.findViewById(R.id.editTextUpdateQuantity);
                    Button btnUpdate = dialogView.findViewById(R.id.buttonUpdate);

                    // تعيين القيم الحالية
                    editClassName.setText(currentClassName);
                    editQuantity.setText(String.valueOf(currentQuantity));

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    btnUpdate.setOnClickListener(innerV -> {
                        String newClassName = editClassName.getText().toString().trim();
                        String newQuantity = editQuantity.getText().toString().trim();

                        if (!newClassName.isEmpty() && !newQuantity.isEmpty()) {
                            try {
                                updateItem(newClassName, newQuantity, itemId); // تحديد ID للسجل
                                dialog.dismiss(); // إغلاق الحوار بعد التعديل
                            } catch (NumberFormatException e) {
                                Toast.makeText(MainActivity.this, "حدث خطأ أثناء التعديل", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.show();
                } else {
                    Toast.makeText(this, "خطأ في البيانات المخزنة", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "اختر عنصرًا لتعدíله", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear All Button Listener
        buttonClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("تأكيد المسح")
                    .setMessage("هل أنت متأكد من أنك تريد مسح جميع الصنوف؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        dbHelper.clearAllItems();
                        itemsList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "تم مسح جميع الصنوف", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("لا", null)
                    .show();
        });

        // Long Click Listener for Deleting Items
        listViewItems.setOnItemLongClickListener((parent, view, position, id) -> {
            int itemId = (int) id;

            // Show confirmation dialog before deletion
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("تأكيد المسح")
                    .setMessage("هل أنت متأكد من أنك تريد مسح هذا الصنف؟")
                    .setPositiveButton("نعم", (dialog, which) -> {
                        if (dbHelper.deleteItem(itemId)) {
                            Toast.makeText(MainActivity.this, "تم المسح بنجاح", Toast.LENGTH_SHORT).show();
                            loadItemsFromDatabase(); // Reload data after deletion
                        } else {
                            Toast.makeText(MainActivity.this, "فشل المسح", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("لا", null)
                    .show();

            return true;
        });
    }

    /**
     * Method to Save Item to Database
     */
    private void saveItemToDatabase() {
        String className = editTextClassName.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString().trim();

        if (!className.isEmpty() && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                boolean isInserted = dbHelper.insertItem(className, quantityStr);

                if (isInserted) {
                    Toast.makeText(this, "تم الحفظ بنجاح", Toast.LENGTH_SHORT).show();
                    loadItemsFromDatabase(); // Reload data after insertion
                } else {
                    Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "يرجى إدخال عدد صحيح", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "يرجى إدخال جميع البيانات", Toast.LENGTH_SHORT).show();
        }

        // Clear input fields
        editTextClassName.setText("");
        editTextQuantity.setText("");
    }

    /**
     * Method to Load Items from Database
     */
    private void loadItemsFromDatabase() {
        Cursor cursor = dbHelper.getAllItems();
        itemsList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String className = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CLASS_NAME));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY));

                    // حفظ ID مع اسم الصنف والعدد
                    itemsList.add(id + " - " + className + " - " + quantity);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error: Missing column in database");
                }
            } while (cursor.moveToNext());

            cursor.close(); // Always close the cursor after use
        }

        adapter.notifyDataSetChanged(); // Update ListView
    }

    /**
     * Method to Update an Item in the Database
     */
    private void updateItem(String className, String quantity, int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CLASS_NAME, className);
        values.put(DatabaseHelper.COLUMN_QUANTITY, Integer.parseInt(quantity));

        long result = db.update(DatabaseHelper.TABLE_ITEMS, values, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (result != 0) { // تأكد من أن التعديل نجح
            Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show();
            loadItemsFromDatabase(); // Reload data after update
        } else {
            Toast.makeText(this, "فشل التعديل", Toast.LENGTH_SHORT).show();
        }
    }


}